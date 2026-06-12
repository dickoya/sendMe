import React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App.jsx';

// Styles globaux
import './assets/styles/global.css';

// Configuration i18n
import './i18n';

createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <App />
    </React.StrictMode>
);