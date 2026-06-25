#!/usr/bin/env node
/**
 * Script de correction des contrastes en mode sombre (version simple)
 * 
 * Ce script remplace automatiquement toutes les classes Tailwind avec
 * des contrastes insuffisants par leurs équivalents améliorés.
 * 
 * Exécution: node scripts/fix-dark-contrast-simple.mjs
 */

import fs from 'fs';
import path from 'path';
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
 * Recherche récursive de fichiers .tsx
 */
function findTsxFiles(dir, fileList = []) {
    const files = fs.readdirSync(dir);
    
    files.forEach(file => {
        const filePath = path.join(dir, file);
        const stat = fs.statSync(filePath);
        
        if (stat.isDirectory()) {
            // Ignore node_modules et dist
            if (!file.startsWith('.') && file !== 'node_modules' && file !== 'dist') {
                findTsxFiles(filePath, fileList);
            }
        } else if (file.endsWith('.tsx')) {
            fileList.push(filePath);
        }
    });
    
    return fileList;
}

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
            
            const relativePath = path.relative(path.join(__dirname, '..'), filePath);
            console.log(`  ✓ ${relativePath}: ${count} × ${description}`);
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
function main() {
    console.log('🎨 Correction des contrastes en mode sombre\n');
    console.log('📁 Recherche des fichiers TSX...\n');
    
    const srcDir = path.join(__dirname, '..', 'src');
    const files = findTsxFiles(srcDir);
    
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
    
    if (Object.keys(stats.replacementsByType).length > 0) {
        console.log('Détails par type:');
        Object.entries(stats.replacementsByType).forEach(([type, count]) => {
            console.log(`  • ${type}: ${count}`);
        });
    } else {
        console.log('ℹ️  Aucune modification nécessaire - tous les fichiers sont déjà à jour!');
    }
    
    console.log('\n' + '='.repeat(60));
    console.log('✅ Correction terminée avec succès!');
    console.log('='.repeat(60) + '\n');
    
    // Affichage des recommandations
    if (stats.filesModified > 0) {
        console.log('📌 PROCHAINES ÉTAPES:\n');
        console.log('1. Vérifier visuellement l\'application en mode sombre');
        console.log('2. Lancer: npm run dev');
        console.log('3. Tester l\'accessibilité avec Lighthouse');
        console.log('4. Vérifier les ratios de contraste (min 4.5:1)\n');
    }
}

// Exécution
try {
    main();
} catch (error) {
    console.error('❌ Erreur:', error.message);
    process.exit(1);
}
