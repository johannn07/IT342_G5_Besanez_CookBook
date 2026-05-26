import { User, Shield, FileText } from 'lucide-react';

export const NAV_ITEMS = [
    { id: 'account', label: 'Account', icon: User },
    { id: 'security', label: 'Security', icon: Shield },
    { id: 'legal', label: 'Legal', icon: FileText },
];

export const LEGAL_LINKS = [
    { label: 'Privacy Policy', path: '/privacy', desc: 'How we handle your data.' },
    { label: 'Terms of Service', path: '/terms', desc: 'Rules for using CookBook.' },
    { label: 'About CookBook', path: '/about', desc: 'Our story and mission.' },
    { label: 'Contact Us', href: 'mailto:support@cookbook.app', desc: 'Get help or send feedback.' },
];