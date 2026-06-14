import { Outlet } from 'react-router-dom';
import LanguageSwitcher from '@/components/layout/LanguageSwitcher';

function PublicLayout() {
    return (
        <div className="flex min-h-screen flex-col bg-gray-50">
            <header className="flex h-14 items-center justify-end px-4 md:px-6">
                <LanguageSwitcher />
            </header>
            <main className="flex flex-1 items-center justify-center px-4 pb-12">
                <Outlet />
            </main>
        </div>
    );
}

export default PublicLayout;