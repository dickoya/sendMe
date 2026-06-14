/**
 * Vérifie que tous les fichiers de traduction fr/*.json et en/*.json
 * possèdent exactement les mêmes clés (récursivement).
 *
 * Usage : node scripts/check-i18n.js
 * Code de sortie 1 si une divergence est détectée (utilisable en CI).
 */
const fs = require('fs');
const path = require('path');

const LOCALES_DIR = path.join(__dirname, '..', 'src', 'i18n', 'locales');
const LANGUAGES = ['fr', 'en'];

/**
 * Retourne la liste de toutes les clés "à plat" d'un objet,
 * avec notation pointée pour les objets imbriqués.
 * Ex: { a: { b: 1 } } → ["a.b"]
 */
function flattenKeys(obj, prefix = '') {
    return Object.entries(obj).flatMap(([key, value]) => {
        const fullKey = prefix ? `${prefix}.${key}` : key;
        if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
            return flattenKeys(value, fullKey);
        }
        return [fullKey];
    });
}

function main() {
    const namespaces = fs
        .readdirSync(path.join(LOCALES_DIR, 'fr'))
        .filter((f) => f.endsWith('.json'))
        .map((f) => f.replace('.json', ''));

    let hasErrors = false;

    for (const ns of namespaces) {
        const keysByLang = {};

        for (const lang of LANGUAGES) {
            const filePath = path.join(LOCALES_DIR, lang, `${ns}.json`);
            if (!fs.existsSync(filePath)) {
                console.error(`❌ [${ns}] Fichier manquant : ${filePath}`);
                hasErrors = true;
                continue;
            }
            const content = JSON.parse(fs.readFileSync(filePath, 'utf-8'));
            keysByLang[lang] = new Set(flattenKeys(content));
        }

        const [first, ...rest] = LANGUAGES;
        for (const lang of rest) {
            const missingInLang = [...keysByLang[first]].filter((k) => !keysByLang[lang].has(k));
            const missingInFirst = [...keysByLang[lang]].filter((k) => !keysByLang[first].has(k));

            if (missingInLang.length > 0) {
                hasErrors = true;
                console.error(`❌ [${ns}] Clés présentes en "${first}" mais absentes en "${lang}" :`);
                missingInLang.forEach((k) => console.error(`   - ${k}`));
            }
            if (missingInFirst.length > 0) {
                hasErrors = true;
                console.error(`❌ [${ns}] Clés présentes en "${lang}" mais absentes en "${first}" :`);
                missingInFirst.forEach((k) => console.error(`   - ${k}`));
            }
        }

        if (!hasErrors) {
            console.log(`✅ [${ns}] OK — clés synchronisées entre ${LANGUAGES.join(', ')}`);
        }
    }

    if (hasErrors) {
        console.error('\n💥 Des divergences ont été détectées entre les fichiers de traduction.');
        process.exit(1);
    } else {
        console.log('\n🎉 Tous les namespaces sont synchronisés entre toutes les langues.');
    }
}

main();