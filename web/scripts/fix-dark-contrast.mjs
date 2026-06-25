#!/usr/bin/env node
/**
 * Script de correction des contrastes en mode sombre
 * 
 * Ce script remplace automatiquement toutes les classes Tailwind avec
 * des contrastes insuffisants par leurs équivalents améliorés.
 * 
 * Exécution: node scripts/fix-dark-contrast.mjs
 */

import fs from 'fs';
import path from 'path';
import { glob } from 'glob';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Configuration des remplacements
const replacements = [
    {
        pattern: /dark:text-gray-400/g,
        replacement: 'dark:text-gray-300',
        description: 'Amélioration contraste gray-400 → gray-300'
    },
    {
        pattern: /dark:text-gray-500/g,
        replacement: 'dark:text-gray-400',
        description: 'Amélioration contraste gray-500 → gray-400'
    }
];

// Statistiques
const stats = {
    filesProcessed: 0,
    filesModified: 0,
    totalReplacements: 0,
    replacementsByType: {}
};

/**
 * Traite un fichier et applique les remplacements
 */
function processFile(filePath) {
    const content = fs.readFileSync(filePath, 'utf8');
    let modifiedContent = content;
    let fileModified = false;
    
    replacements.forEach(({ pattern, replacement, description }) => {
        const matches = content.match(pattern);
        if (matches) {
            modifiedContent = modifiedContent.replace(pattern, replacement);
            fileModified = true;
            
            const count = matches.length;
            stats.totalReplacements += count;
            stats.replacementsByType[description] = (stats.replacementsByType[description] || 0) + count;
            
            console.log(`  ✓ ${path.relative(process.cwd(), filePath)}: ${count} × ${description}`);
        }
    });
    
    if (fileModified) {
        fs.writeFileSync(filePath, modifiedContent, 'utf8');
        stats.filesModified++;
    }
    
    stats.filesProcessed++;
}

/**
 * Point d'entrée principal
 */
async function main() {
    console.log('🎨 Correction des contrastes en mode sombre\n');
    console.log('📁 Recherche des fichiers TSX...\n');
    
    // Recherche tous les fichiers .tsx dans src/
    const files = await glob('src/**/*.tsx', {
        cwd: path.join(__dirname, '..'),
        absolute: true
    });
    
    console.log(`📝 ${files.length} fichiers trouvés\n`);
    console.log('🔄 Traitement en cours...\n');
    
    files.forEach(processFile);
    
    // Affichage du rapport
    console.log('\n' + '='.repeat(60));
    console.log('📊 RAPPORT DES MODIFICATIONS');
    console.log('='.repeat(60));
    console.log(`\n📂 Fichiers traités: ${stats.filesProcessed}`);
    console.log(`✏️  Fichiers modifiés: ${stats.filesModified}`);
    console.log(`🔧 Total de remplacements: ${stats.totalReplacements}\n`);
    
    console.log('Détails par type:');
    Object.entries(stats.replacementsByType).forEach(([type, count]) => {
        console.log(`  • ${type}: ${count}`);
    });
    
    console.log('\n' + '='.repeat(60));
    console.log('✅ Correction terminée avec succès!');
    console.log('='.repeat(60) + '\n');
    
    // Affichage des recommandations
    console.log('📌 PROCHAINES ÉTAPES:\n');
    console.log('1. Vérifier visuellement l\'application en mode sombre');
    console.log('2. Lancer: npm run dev');
    console.log('3. Tester l\'accessibilité avec Lighthouse');
    console.log('4. Vérifier les ratios de contraste (min 4.5:1)\n');
}

// Exécution
main().catch(error => {
    console.error('❌ Erreur:', error);
    process.exit(1);
});
