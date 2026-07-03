import { useState, type ReactNode } from 'react';
import { cn } from '@/lib/utils';

export interface Tab {
    id: string;
    label: string;
    icon?: ReactNode;
    content: ReactNode;
    disabled?: boolean;
}

export interface TabsProps {
    tabs: Tab[];
    defaultTab?: string;
    onChange?: (tabId: string) => void;
    className?: string;
}

/**
 * Navigation par onglets.
 *
 * @example
 * <Tabs
 *   tabs={[
 *     { id: 'info', label: 'Informations', content: <InfoForm /> },
 *     { id: 'password', label: 'Mot de passe', content: <PasswordForm /> },
 *   ]}
 * />
 */
function Tabs({ tabs, defaultTab, onChange, className }: TabsProps) {
    const [activeTab, setActiveTab] = useState(defaultTab ?? tabs[0]?.id);

    const handleTabChange = (tabId: string) => {
        setActiveTab(tabId);
        onChange?.(tabId);
    };

    const activeContent = tabs.find((t) => t.id === activeTab)?.content;

    return (
        <div className={cn('flex flex-col gap-4', className)}>
            {/* Tab bar */}
            <div className="flex border-b border-gray-100 dark:border-[#222222]">
                {tabs.map((tab) => (
                    <button
                        key={tab.id}
                        onClick={() => !tab.disabled && handleTabChange(tab.id)}
                        disabled={tab.disabled}
                        className={cn(
                            'flex items-center gap-2 px-4 py-3 text-sm font-medium transition-colors border-b-2 -mb-px',
                            activeTab === tab.id
                                ? 'border-brand-600 text-brand-600 dark:border-brand-400 dark:text-brand-400'
                                : 'border-transparent text-gray-500 hover:text-gray-700 dark:text-[#8B8FA3] dark:hover:text-[#EDEEF0]',
                            tab.disabled && 'opacity-40 cursor-not-allowed'
                        )}
                    >
                        {tab.icon && <span>{tab.icon}</span>}
                        {tab.label}
                    </button>
                ))}
            </div>

            {/* Tab content */}
            <div className="pt-1">
                {activeContent}
            </div>
        </div>
    );
}

export default Tabs;
