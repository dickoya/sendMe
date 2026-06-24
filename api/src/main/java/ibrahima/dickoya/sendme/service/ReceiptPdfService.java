package ibrahima.dickoya.sendme.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import ibrahima.dickoya.sendme.entity.BeneficiaryChannel;
import ibrahima.dickoya.sendme.entity.Transaction;
import ibrahima.dickoya.sendme.enums.ChannelType;
import ibrahima.dickoya.sendme.enums.TransactionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Service générant les reçus PDF pour chaque transaction complétée.
 *
 * Utilise la librairie iText7 pour créer des PDFs professionnels
 * contenant toutes les informations de la transaction :
 * - En-tête avec logo et titre
 * - Informations de l'expéditeur et du bénéficiaire
 * - Détails financiers (montant, frais, taux de change)
 * - Pied de page avec mentions légales
 *
 * Le PDF est généré en mémoire (ByteArrayOutputStream) et
 * retourné directement dans la réponse HTTP sans stockage disque.
 * Cela évite d'accumuler des fichiers sur le serveur.
 */
@Service
@Slf4j
public class ReceiptPdfService {

        // ── Couleurs de la charte graphique SendMe ──────────────────────────────

        /**
         * Couleur principale — bleu profond SendMe.
         * Utilisée pour l'en-tête et les titres principaux.
         */
        private static final DeviceRgb COLOR_PRIMARY = new DeviceRgb(27, 79, 114);

        /**
         * Couleur secondaire — bleu moyen.
         * Utilisée pour les sous-titres et séparateurs.
         */
        private static final DeviceRgb COLOR_SECONDARY = new DeviceRgb(46, 134, 193);

        /**
         * Couleur succès — vert.
         * Utilisée pour afficher le statut COMPLETED.
         */
        private static final DeviceRgb COLOR_SUCCESS = new DeviceRgb(21, 143, 119);

        /**
         * Couleur danger — rouge.
         * Utilisée pour afficher les statuts FAILED / REFUNDED.
         */
        private static final DeviceRgb COLOR_DANGER = new DeviceRgb(192, 57, 43);

        /**
         * Couleur fond gris clair.
         * Utilisée pour les cellules de tableau alternées.
         */
        private static final DeviceRgb COLOR_LIGHT_GRAY = new DeviceRgb(242, 243, 244);

        /**
         * Couleur texte secondaire — gris moyen.
         * Utilisée pour les libellés et informations secondaires.
         */
        private static final DeviceRgb COLOR_TEXT_SECONDARY = new DeviceRgb(85, 85, 85);

        /**
         * Formatage des dates dans le PDF.
         * Exemple : "09 juin 2025 à 14:32:15"
         */
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
                        .ofPattern("dd MMMM yyyy 'à' HH:mm:ss");

        /**
         * Génère le reçu PDF d'une transaction.
         *
         * Processus de génération :
         * 1. Créer un document PDF en mémoire
         * 2. Ajouter l'en-tête (logo + titre + référence)
         * 3. Ajouter la bannière de statut (COMPLETED / FAILED)
         * 4. Ajouter les informations des parties (expéditeur + bénéficiaire)
         * 5. Ajouter le tableau des détails financiers
         * 6. Ajouter les informations Stripe
         * 7. Ajouter le pied de page légal
         * 8. Retourner le PDF sous forme de tableau d'octets
         *
         * @param transaction la transaction dont on génère le reçu
         * @return byte[] le contenu du PDF prêt à télécharger
         */
        public byte[] generateReceipt(Transaction transaction) {

                log.info("[PDF] Génération du reçu pour : {}", transaction.getReference());

                // Créer un flux mémoire pour stocker le PDF
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                try {
                        // Initialiser le document PDF
                        PdfWriter writer = new PdfWriter(outputStream);
                        PdfDocument pdf = new PdfDocument(writer);
                        Document document = new Document(pdf, PageSize.A4);

                        // Marges du document (gauche, droite, haut, bas) en points
                        document.setMargins(40, 40, 40, 40);

                        // ── Section 1 : En-tête ──────────────────────────────────────────
                        addHeader(document, transaction);

                        // ── Section 2 : Bannière de statut ──────────────────────────────
                        addStatusBanner(document, transaction);

                        // ── Section 3 : Informations des parties ────────────────────────
                        addPartiesSection(document, transaction);

                        // ── Section 4 : Détails financiers ──────────────────────────────
                        addFinancialDetails(document, transaction);

                        // ── Section 5 : Informations Stripe ─────────────────────────────
                        addStripeInfo(document, transaction);

                        // ── Section 6 : Pied de page ─────────────────────────────────────
                        addFooter(document, transaction);

                        // Fermer le document et récupérer les octets
                        document.close();

                } catch (Exception e) {
                        log.error("[PDF] Erreur lors de la génération : {}", e.getMessage());
                        throw new RuntimeException("Erreur génération PDF : " + e.getMessage());
                }

                log.info("[PDF] Reçu généré avec succès pour : {}", transaction.getReference());
                return outputStream.toByteArray();
        }

        /**
         * Ajoute l'en-tête du reçu avec le nom de l'application,
         * le titre du document et la référence de la transaction.
         *
         * @param document    le document PDF en cours de construction
         * @param transaction la transaction concernée
         */
        private void addHeader(Document document, Transaction transaction) {

                // ── Ligne de titre principale ────────────────────────────────────────
                // Tableau à 2 colonnes : logo à gauche, info à droite
                Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }))
                                .setWidth(UnitValue.createPercentValue(100));

                // Colonne gauche : nom de l'application
                Cell logoCell = new Cell()
                                .setBorder(Border.NO_BORDER)
                                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);

                Paragraph appName = new Paragraph("SendMe")
                                .setFontSize(28)
                                .setFontColor(COLOR_PRIMARY)
                                .setBold()
                                .setMargin(0);

                Paragraph appTagline = new Paragraph("Passerelle de transfert d'argent")
                                .setFontSize(10)
                                .setFontColor(COLOR_TEXT_SECONDARY)
                                .setMargin(0);

                logoCell.add(appName).add(appTagline);
                headerTable.addCell(logoCell);

                // Colonne droite : titre du document + référence
                Cell titleCell = new Cell()
                                .setBorder(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);

                Paragraph receiptTitle = new Paragraph("RECU DE TRANSFERT")
                                .setFontSize(14)
                                .setFontColor(COLOR_SECONDARY)
                                .setBold()
                                .setMargin(0);

                Paragraph refParagraph = new Paragraph(transaction.getReference())
                                .setFontSize(12)
                                .setFontColor(COLOR_PRIMARY)
                                .setBold()
                                .setMargin(0);

                Paragraph dateParagraph = new Paragraph(
                                transaction.getCreatedAt().format(DATE_FORMATTER))
                                .setFontSize(9)
                                .setFontColor(COLOR_TEXT_SECONDARY)
                                .setMargin(0);

                titleCell.add(receiptTitle).add(refParagraph).add(dateParagraph);
                headerTable.addCell(titleCell);

                document.add(headerTable);

                // Ligne de séparation bleue sous l'en-tête
                document.add(new LineSeparator(
                                new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(2f))
                                .setStrokeColor(COLOR_PRIMARY)
                                .setMarginTop(8)
                                .setMarginBottom(16));
        }

        /**
         * Ajoute une bannière colorée indiquant le statut de la transaction.
         * Vert pour COMPLETED, Rouge pour FAILED/REFUNDED, Orange pour les autres.
         *
         * @param document    le document PDF
         * @param transaction la transaction concernée
         */
        private void addStatusBanner(Document document, Transaction transaction) {

                // Choisir la couleur et le texte selon le statut
                DeviceRgb bannerColor;
                String statusText;
                String statusIcon;

                if (transaction.getStatus() == TransactionStatus.COMPLETED) {
                        bannerColor = COLOR_SUCCESS;
                        statusText = "TRANSFERT COMPLETE AVEC SUCCES";
                        statusIcon = "✓ ";
                } else if (transaction.getStatus() == TransactionStatus.FAILED) {
                        bannerColor = COLOR_DANGER;
                        statusText = "TRANSFERT ECHOUE";
                        statusIcon = "✗ ";
                } else if (transaction.getStatus() == TransactionStatus.REFUNDED) {
                        bannerColor = new DeviceRgb(142, 68, 173); // violet
                        statusText = "TRANSFERT REMBOURSE";
                        statusIcon = "↩ ";
                } else {
                        bannerColor = new DeviceRgb(230, 126, 34); // orange
                        statusText = "TRANSFERT EN COURS";
                        statusIcon = "⟳ ";
                }

                // Créer la bannière colorée
                Table statusTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                                .setWidth(UnitValue.createPercentValue(100));

                Cell statusCell = new Cell()
                                .setBackgroundColor(bannerColor)
                                .setPadding(10)
                                .setBorder(Border.NO_BORDER)
                                .setTextAlignment(TextAlignment.CENTER);

                statusCell.add(new Paragraph(statusIcon + statusText)
                                .setFontColor(ColorConstants.WHITE)
                                .setFontSize(13)
                                .setBold()
                                .setMargin(0));

                statusTable.addCell(statusCell);
                document.add(statusTable);
                document.add(new Paragraph("").setMarginBottom(12));
        }

        /**
         * Ajoute la section des informations des deux parties :
         * l'expéditeur (à gauche) et le bénéficiaire (à droite).
         *
         * @param document    le document PDF
         * @param transaction la transaction concernée
         */
        private void addPartiesSection(Document document, Transaction transaction) {

                // Titre de section
                document.add(createSectionTitle("INFORMATIONS DES PARTIES"));

                // Tableau à 2 colonnes : expéditeur | bénéficiaire
                Table partiesTable = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }))
                                .setWidth(UnitValue.createPercentValue(100))
                                .setMarginBottom(16);

                // ── Colonne gauche : Expéditeur ──────────────────────────────────────
                Cell senderCell = new Cell()
                                .setBackgroundColor(COLOR_LIGHT_GRAY)
                                .setPadding(12)
                                .setBorder(new SolidBorder(COLOR_SECONDARY, 0.5f));

                senderCell.add(new Paragraph("EXPEDITEUR")
                                .setFontSize(9)
                                .setFontColor(COLOR_TEXT_SECONDARY)
                                .setBold()
                                .setMarginBottom(6));

                senderCell.add(new Paragraph(
                                transaction.getSender().getFirstName() + " " +
                                                transaction.getSender().getLastName())
                                .setFontSize(12)
                                .setFontColor(COLOR_PRIMARY)
                                .setBold()
                                .setMarginBottom(2));

                senderCell.add(new Paragraph(transaction.getSender().getEmail())
                                .setFontSize(10)
                                .setFontColor(COLOR_TEXT_SECONDARY)
                                .setMarginBottom(2));

                if (transaction.getSender().getPhoneNumber() != null) {
                        senderCell.add(new Paragraph(transaction.getSender().getPhoneNumber())
                                        .setFontSize(10)
                                        .setFontColor(COLOR_TEXT_SECONDARY));
                }

                partiesTable.addCell(senderCell);

                // ── Colonne droite : Bénéficiaire ────────────────────────────────────
                Cell beneficiaryCell = new Cell()
                                .setBackgroundColor(COLOR_LIGHT_GRAY)
                                .setPadding(12)
                                .setBorder(new SolidBorder(COLOR_SECONDARY, 0.5f));

                beneficiaryCell.add(new Paragraph("BENEFICIAIRE")
                                .setFontSize(9)
                                .setFontColor(COLOR_TEXT_SECONDARY)
                                .setBold()
                                .setMarginBottom(6));

                beneficiaryCell.add(new Paragraph(
                                transaction.getBeneficiary().getFirstName() + " " +
                                                transaction.getBeneficiary().getLastName())
                                .setFontSize(12)
                                .setFontColor(COLOR_PRIMARY)
                                .setBold()
                                .setMarginBottom(2));

                // Canal de réception
                beneficiaryCell.add(new Paragraph(
                                "Canal : " + transaction.getChannel().name().replace("_", " "))
                                .setFontSize(10)
                                .setFontColor(COLOR_TEXT_SECONDARY)
                                .setMarginBottom(2));

                // Détails selon le canal - utiliser le canal principal du bénéficiaire
                var primaryChannel = transaction.getBeneficiary().getChannels().stream()
                                .filter(BeneficiaryChannel::getIsPrimary)
                                .findFirst()
                                .or(() -> transaction.getBeneficiary().getChannels().stream().findFirst());

                if (primaryChannel.isPresent()) {
                        var channel = primaryChannel.get();
                        if (channel.getChannel() == ChannelType.BANK_TRANSFER) {
                                beneficiaryCell.add(new Paragraph(
                                                "Banque : " + channel.getBankName())
                                                .setFontSize(10)
                                                .setFontColor(COLOR_TEXT_SECONDARY)
                                                .setMarginBottom(2));
                                // Masquer l'IBAN pour la sécurité
                                String iban = channel.getIban();
                                beneficiaryCell.add(new Paragraph("IBAN : " + maskIban(iban))
                                                .setFontSize(10)
                                                .setFontColor(COLOR_TEXT_SECONDARY));
                        } else {
                                String phone = channel.getPhoneNumber();
                                beneficiaryCell.add(new Paragraph("Tel. : " + phone)
                                                .setFontSize(10)
                                                .setFontColor(COLOR_TEXT_SECONDARY));
                        }
                }

                partiesTable.addCell(beneficiaryCell);
                document.add(partiesTable);
        }

        /**
         * Ajoute le tableau des détails financiers de la transaction :
         * montant envoyé, frais, taux de change, montant reçu, total débité.
         *
         * @param document    le document PDF
         * @param transaction la transaction concernée
         */
        private void addFinancialDetails(Document document, Transaction transaction) {

                document.add(createSectionTitle("DETAILS FINANCIERS"));

                // Tableau à 2 colonnes : libellé | valeur
                Table financialTable = new Table(UnitValue.createPercentArray(new float[] { 1.5f, 1 }))
                                .setWidth(UnitValue.createPercentValue(100))
                                .setMarginBottom(16);

                // Ligne 1 : Montant envoyé
                addFinancialRow(financialTable,
                                "Montant envoye",
                                transaction.getAmountSent() + " " + transaction.getCurrencySent(),
                                false);

                // Ligne 2 : Frais de la plateforme
                addFinancialRow(financialTable,
                                "Frais de la plateforme (1.5%)",
                                "+ " + transaction.getFees() + " " + transaction.getCurrencySent(),
                                true);

                // Ligne 3 : Réduction parrainage (si applicable)
                if (transaction.getDiscount() != null
                                && transaction.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                        addFinancialRow(financialTable,
                                        "Reduction de parrainage",
                                        "- " + transaction.getDiscount() + " " + transaction.getCurrencySent(),
                                        false);
                }

                // Ligne 4 : Taux de change
                addFinancialRow(financialTable,
                                "Taux de change applique",
                                "1 " + transaction.getCurrencySent() +
                                                " = " + transaction.getExchangeRate() +
                                                " " + transaction.getCurrencyReceived(),
                                transaction.getDiscount() != null
                                                && transaction.getDiscount().compareTo(BigDecimal.ZERO) > 0);

                // Ligne 5 : Total débité (en gras, mis en évidence)
                // Calcul : amountSent + fees - discount
                BigDecimal totalDebited = transaction.getAmountSent()
                                .add(transaction.getFees());
                if (transaction.getDiscount() != null) {
                        totalDebited = totalDebited.subtract(transaction.getDiscount());
                }
                addFinancialRowHighlighted(financialTable,
                                "TOTAL DEBITE SUR LA CARTE",
                                totalDebited + " " + transaction.getCurrencySent());

                // Ligne 6 : Montant reçu (en vert)
                addFinancialRowSuccess(financialTable,
                                "MONTANT RECU PAR LE BENEFICIAIRE",
                                transaction.getAmountReceived() + " " +
                                                transaction.getCurrencyReceived());

                document.add(financialTable);
        }

        /**
         * Ajoute les informations du paiement Stripe.
         * Permet à l'utilisateur de retrouver le paiement
         * dans son relevé bancaire ou en cas de litige.
         *
         * @param document    le document PDF
         * @param transaction la transaction concernée
         */
        private void addStripeInfo(Document document, Transaction transaction) {

                document.add(createSectionTitle("INFORMATIONS DE PAIEMENT"));

                Table stripeTable = new Table(UnitValue.createPercentArray(new float[] { 1.5f, 1 }))
                                .setWidth(UnitValue.createPercentValue(100))
                                .setMarginBottom(16);

                // Référence SendMe
                addFinancialRow(stripeTable,
                                "Reference SendMe",
                                transaction.getReference(),
                                false);

                // PaymentIntent Stripe
                if (transaction.getStripePaymentIntentId() != null) {
                        addFinancialRow(stripeTable,
                                        "Reference paiement Stripe",
                                        transaction.getStripePaymentIntentId(),
                                        true);
                }

                // Statut Stripe
                if (transaction.getStripePaymentStatus() != null) {
                        addFinancialRow(stripeTable,
                                        "Statut paiement",
                                        transaction.getStripePaymentStatus(),
                                        false);
                }

                // Date de création
                addFinancialRow(stripeTable,
                                "Date du transfert",
                                transaction.getCreatedAt().format(DATE_FORMATTER),
                                true);

                // Date de mise à jour
                if (transaction.getUpdatedAt() != null) {
                        addFinancialRow(stripeTable,
                                        "Derniere mise a jour",
                                        transaction.getUpdatedAt().format(DATE_FORMATTER),
                                        false);
                }

                document.add(stripeTable);
        }

        /**
         * Ajoute le pied de page avec les mentions légales et informations de contact.
         *
         * @param document    le document PDF
         * @param transaction la transaction concernée
         */
        private void addFooter(Document document, Transaction transaction) {

                // Ligne de séparation
                document.add(new LineSeparator(
                                new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f))
                                .setStrokeColor(COLOR_TEXT_SECONDARY)
                                .setMarginTop(16)
                                .setMarginBottom(8));

                // Mentions légales
                document.add(new Paragraph(
                                "Ce document est un recu officiel genere automatiquement par SendMe. " +
                                                "Conservez-le comme preuve de votre transaction.")
                                .setFontSize(8)
                                .setFontColor(COLOR_TEXT_SECONDARY)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(4));

                document.add(new Paragraph(
                                "SendMe — Solution digitale de transfert sécurisé | " +
                                                "support@sendme.com | www.sendme.com")
                                .setFontSize(8)
                                .setFontColor(COLOR_TEXT_SECONDARY)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(4));

                document.add(new Paragraph(
                                "Document genere le " +
                                                java.time.LocalDateTime.now().format(DATE_FORMATTER) +
                                                " | Ref : " + transaction.getReference())
                                .setFontSize(7)
                                .setFontColor(COLOR_TEXT_SECONDARY)
                                .setTextAlignment(TextAlignment.CENTER));
        }

        // ── Méthodes utilitaires ─────────────────────────────────────────────────

        /**
         * Crée un titre de section avec la couleur primaire et une bordure inférieure.
         *
         * @param title le texte du titre
         * @return Paragraph le titre formaté
         */
        private Paragraph createSectionTitle(String title) {
                return new Paragraph(title)
                                .setFontSize(11)
                                .setFontColor(COLOR_PRIMARY)
                                .setBold()
                                .setBorderBottom(new SolidBorder(COLOR_SECONDARY, 1f))
                                .setPaddingBottom(4)
                                .setMarginBottom(8)
                                .setMarginTop(8);
        }

        /**
         * Ajoute une ligne standard dans un tableau financier.
         * Alterne les couleurs de fond pour la lisibilité.
         *
         * @param table       le tableau cible
         * @param label       le libellé (colonne gauche)
         * @param value       la valeur (colonne droite)
         * @param highlighted si true → fond gris clair
         */
        private void addFinancialRow(Table table, String label,
                        String value, boolean highlighted) {
                DeviceRgb bg = highlighted ? COLOR_LIGHT_GRAY : new DeviceRgb(255, 255, 255);

                Cell labelCell = new Cell()
                                .setBackgroundColor(bg)
                                .setPadding(8)
                                .setBorder(new SolidBorder(new DeviceRgb(220, 220, 220), 0.3f));
                labelCell.add(new Paragraph(label)
                                .setFontSize(10)
                                .setFontColor(COLOR_TEXT_SECONDARY)
                                .setMargin(0));

                Cell valueCell = new Cell()
                                .setBackgroundColor(bg)
                                .setPadding(8)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setBorder(new SolidBorder(new DeviceRgb(220, 220, 220), 0.3f));
                valueCell.add(new Paragraph(value)
                                .setFontSize(10)
                                .setFontColor(ColorConstants.BLACK)
                                .setBold()
                                .setMargin(0));

                table.addCell(labelCell);
                table.addCell(valueCell);
        }

        /**
         * Ajoute une ligne mise en évidence (fond bleu) dans un tableau financier.
         * Utilisée pour le total débité.
         *
         * @param table le tableau cible
         * @param label le libellé
         * @param value la valeur
         */
        private void addFinancialRowHighlighted(Table table,
                        String label, String value) {
                Cell labelCell = new Cell()
                                .setBackgroundColor(COLOR_PRIMARY)
                                .setPadding(10)
                                .setBorder(Border.NO_BORDER);
                labelCell.add(new Paragraph(label)
                                .setFontSize(11)
                                .setFontColor(ColorConstants.WHITE)
                                .setBold()
                                .setMargin(0));

                Cell valueCell = new Cell()
                                .setBackgroundColor(COLOR_PRIMARY)
                                .setPadding(10)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setBorder(Border.NO_BORDER);
                valueCell.add(new Paragraph(value)
                                .setFontSize(13)
                                .setFontColor(ColorConstants.WHITE)
                                .setBold()
                                .setMargin(0));

                table.addCell(labelCell);
                table.addCell(valueCell);
        }

        /**
         * Ajoute une ligne verte dans un tableau financier.
         * Utilisée pour le montant reçu par le bénéficiaire.
         *
         * @param table le tableau cible
         * @param label le libellé
         * @param value la valeur
         */
        private void addFinancialRowSuccess(Table table,
                        String label, String value) {
                Cell labelCell = new Cell()
                                .setBackgroundColor(new DeviceRgb(213, 245, 227))
                                .setPadding(10)
                                .setBorder(new SolidBorder(COLOR_SUCCESS, 0.5f));
                labelCell.add(new Paragraph(label)
                                .setFontSize(11)
                                .setFontColor(COLOR_SUCCESS)
                                .setBold()
                                .setMargin(0));

                Cell valueCell = new Cell()
                                .setBackgroundColor(new DeviceRgb(213, 245, 227))
                                .setPadding(10)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setBorder(new SolidBorder(COLOR_SUCCESS, 0.5f));
                valueCell.add(new Paragraph(value)
                                .setFontSize(14)
                                .setFontColor(COLOR_SUCCESS)
                                .setBold()
                                .setMargin(0));

                table.addCell(labelCell);
                table.addCell(valueCell);
        }

        /**
         * Masque un IBAN pour la sécurité dans le PDF.
         * Montre seulement les 4 premiers et 4 derniers caractères.
         * Exemple : FR7630006000011234567890189 → FR76 **** **** 0189
         *
         * @param iban l'IBAN complet
         * @return l'IBAN masqué
         */
        private String maskIban(String iban) {
                if (iban == null || iban.length() < 8)
                        return "****";
                return iban.substring(0, 4) + " **** **** " +
                                iban.substring(iban.length() - 4);
        }
}