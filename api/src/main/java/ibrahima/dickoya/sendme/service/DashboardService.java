package ibrahima.dickoya.sendme.service;

import ibrahima.dickoya.sendme.config.CurrencyConfig;
import ibrahima.dickoya.sendme.dto.response.LiveExchangeRateResponse;
import ibrahima.dickoya.sendme.dto.response.TransferEvolutionResponse;
import ibrahima.dickoya.sendme.dto.response.UserDashboardStatsResponse;
import ibrahima.dickoya.sendme.entity.ExchangeRate;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.entity.User;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import ibrahima.dickoya.sendme.repository.ExchangeRateRepository;
import ibrahima.dickoya.sendme.repository.TransactionRepository;
import ibrahima.dickoya.sendme.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service gérant les données du tableau de bord utilisateur.
 * <p>
 * Fournit :
 * - Les statistiques personnelles (total envoyé, économies, bénéficiaire
 * favori)
 * - L'évolution des transferts (mensuel/annuel)
 * - Les taux de change en direct avec historique
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

        private final UserRepository userRepository;
        private final TransactionRepository transactionRepository;
        private final ExchangeRateRepository exchangeRateRepository;
        private final ExchangeRateService exchangeRateService;

        /**
         * Récupère les statistiques personnelles de l'utilisateur connecté.
         */
        public UserDashboardStatsResponse getStats(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));

                Long userId = user.getId();

                // Total des transactions
                Long totalTransactions = transactionRepository.countBySenderId(userId);

                // Transactions complétées
                Long completedTransactions = transactionRepository
                                .countByStatus(TransactionStatus.COMPLETED);

                // Montant total envoyé (COMPLETED)
                Double totalSent = transactionRepository
                                .sumAmountSentBySenderIdAndStatus(userId, TransactionStatus.COMPLETED);
                totalSent = totalSent != null ? totalSent : 0.0;

                // Total des frais payés par l'utilisateur
                Double totalFees = calculateTotalFeesForUser(userId);
                totalFees = totalFees != null ? totalFees : 0.0;

                // Montant total reçu (estimation basée sur le montant envoyé - frais)
                Double totalReceived = totalSent - totalFees;

                // Économies estimées (2% d'économie vs concurrents traditionnels)
                Double savings = totalFees * 0.5; // Estimation : 50% d'économie vs Western Union

                // Bénéficiaire le plus fréquent
                UserDashboardStatsResponse.FavoriteBeneficiary favorite = findFavoriteBeneficiary(userId);

                return UserDashboardStatsResponse.builder()
                                .totalSent(totalSent)
                                .totalReceived(totalReceived)
                                .totalFees(totalFees)
                                .totalTransactions(totalTransactions)
                                .completedTransactions(completedTransactions)
                                .savings(savings)
                                .favoriteBeneficiary(favorite)
                                .build();
        }

        /**
         * Récupère l'évolution mensuelle et annuelle des transferts.
         */
        public TransferEvolutionResponse getTransferEvolution(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));

                List<Transaction> transactions = transactionRepository
                                .findBySenderIdOrderByCreatedAtDesc(user.getId(),
                                                org.springframework.data.domain.Pageable.unpaged())
                                .getContent();

                // Évolution mensuelle
                Map<String, List<Transaction>> byMonth = transactions.stream()
                                .collect(Collectors.groupingBy(tx -> tx.getCreatedAt()
                                                .format(DateTimeFormatter.ofPattern("yyyy-MM"))));

                List<TransferEvolutionResponse.MonthlyTransfer> monthly = byMonth.entrySet().stream()
                                .map(entry -> {
                                        String month = entry.getKey();
                                        List<Transaction> txList = entry.getValue();
                                        long count = txList.size();
                                        double total = txList.stream()
                                                        .filter(tx -> tx.getAmountSent() != null)
                                                        .mapToDouble(tx -> tx.getAmountSent().doubleValue())
                                                        .sum();
                                        return TransferEvolutionResponse.MonthlyTransfer.builder()
                                                        .month(month)
                                                        .count(count)
                                                        .totalAmount(total)
                                                        .build();
                                })
                                .sorted(Comparator.comparing(TransferEvolutionResponse.MonthlyTransfer::getMonth))
                                .collect(Collectors.toList());

                // Évolution annuelle
                Map<Integer, List<Transaction>> byYear = transactions.stream()
                                .collect(Collectors.groupingBy(tx -> tx.getCreatedAt().getYear()));

                List<TransferEvolutionResponse.YearlyTransfer> yearly = byYear.entrySet().stream()
                                .map(entry -> {
                                        int year = entry.getKey();
                                        List<Transaction> txList = entry.getValue();
                                        long count = txList.size();
                                        double total = txList.stream()
                                                        .filter(tx -> tx.getAmountSent() != null)
                                                        .mapToDouble(tx -> tx.getAmountSent().doubleValue())
                                                        .sum();
                                        return TransferEvolutionResponse.YearlyTransfer.builder()
                                                        .year(year)
                                                        .count(count)
                                                        .totalAmount(total)
                                                        .build();
                                })
                                .sorted(Comparator.comparing(TransferEvolutionResponse.YearlyTransfer::getYear))
                                .collect(Collectors.toList());

                return TransferEvolutionResponse.builder()
                                .monthly(monthly)
                                .yearly(yearly)
                                .build();
        }

        /**
         * Récupère les taux de change en direct pour toutes les paires supportées.
         * Les paires sont définies dans CurrencyConfig.getExchangeRatePairs().
         */
        public List<LiveExchangeRateResponse> getLiveExchangeRates() {
                List<String[]> pairs = CurrencyConfig.getExchangeRatePairs();

                List<LiveExchangeRateResponse> rates = new ArrayList<>();
                for (String[] pair : pairs) {
                        String from = pair[0];
                        String to = pair[1];

                        BigDecimal rate = exchangeRateService.getRate(from, to);
                        BigDecimal rawRate = exchangeRateService.getRawRate(from, to);
                        String timestamp = exchangeRateService.getCacheTimestamp(from);

                        rates.add(LiveExchangeRateResponse.builder()
                                        .from(from)
                                        .to(to)
                                        .rate(rate != null ? rate.doubleValue() : 0.0)
                                        .rawRate(rawRate != null ? rawRate.doubleValue() : 0.0)
                                        .timestamp(timestamp)
                                        .build());
                }

                return rates;
        }

        /**
         * Récupère l'historique des taux de change pour un couple de devises
         * sur les 7 derniers jours.
         */
        public List<LiveExchangeRateResponse.ExchangeRateHistoryPoint> getExchangeRateHistory(String from, String to) {
                LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
                List<ExchangeRate> rates = exchangeRateRepository
                                .findByBaseCurrencyAndTargetCurrencyAndFetchedAtBetweenOrderByFetchedAtDesc(
                                                from.toUpperCase(), to.toUpperCase(), weekAgo, LocalDateTime.now());

                // Inverser pour avoir du plus ancien au plus récent
                Collections.reverse(rates);

                return rates.stream()
                                .map(rate -> LiveExchangeRateResponse.ExchangeRateHistoryPoint.builder()
                                                .date(rate.getFetchedAt().toString())
                                                .rate(rate.getMarginRate().doubleValue())
                                                .build())
                                .collect(Collectors.toList());
        }

        /**
         * Calcule le total des frais payés par un utilisateur.
         */
        private Double calculateTotalFeesForUser(Long userId) {
                List<Transaction> userTransactions = transactionRepository
                                .findBySenderIdOrderByCreatedAtDesc(userId,
                                                org.springframework.data.domain.Pageable.unpaged())
                                .getContent();

                return userTransactions.stream()
                                .filter(tx -> tx.getStatus() == TransactionStatus.COMPLETED)
                                .filter(tx -> tx.getFees() != null)
                                .mapToDouble(tx -> tx.getFees().doubleValue())
                                .sum();
        }

        /**
         * Trouve le bénéficiaire le plus fréquent de l'utilisateur.
         */
        private UserDashboardStatsResponse.FavoriteBeneficiary findFavoriteBeneficiary(Long userId) {
                List<Transaction> transactions = transactionRepository
                                .findBySenderIdOrderByCreatedAtDesc(userId,
                                                org.springframework.data.domain.Pageable.unpaged())
                                .getContent();

                Map<Long, List<Transaction>> byBeneficiary = transactions.stream()
                                .filter(tx -> tx.getBeneficiary() != null)
                                .collect(Collectors.groupingBy(tx -> tx.getBeneficiary().getId()));

                Optional<Map.Entry<Long, List<Transaction>>> top = byBeneficiary.entrySet().stream()
                                .max(Comparator.comparingInt(entry -> entry.getValue().size()));

                if (top.isPresent()) {
                        Map.Entry<Long, List<Transaction>> entry = top.get();
                        Long beneficiaryId = entry.getKey();
                        long count = entry.getValue().size();
                        var beneficiary = entry.getValue().get(0).getBeneficiary();

                        return UserDashboardStatsResponse.FavoriteBeneficiary.builder()
                                        .id(beneficiaryId)
                                        .firstName(beneficiary.getFirstName())
                                        .lastName(beneficiary.getLastName())
                                        .count(count)
                                        .build();
                }

                return null;
        }
}
