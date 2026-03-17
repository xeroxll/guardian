import React, { createContext, useContext, useState, useEffect, useRef, useCallback, useMemo } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as Device from 'expo-device';
import * as Notifications from 'expo-notifications';
import * as IntentLauncher from 'expo-intent-launcher';
import { Platform } from 'react-native';

export interface BlacklistedApp {
  id: string;
  name: string;
  packageName: string;
}

export interface LogEntry {
  id: string;
  type: 'block' | 'threat' | 'check';
  title: string;
  desc: string;
  time: string;
}

interface GuardianContextType {
  isProtectionEnabled: boolean;
  toggleProtection: () => Promise<void>;
  blacklist: BlacklistedApp[];
  addToBlacklist: (app: Omit<BlacklistedApp, 'id'>) => Promise<void>;
  removeFromBlacklist: (id: string) => Promise<void>;
  logs: LogEntry[];
  usbStatus: boolean;
  toggleUsbStatus: () => Promise<void>;
  stats: {
    threats: number;
    blocks: number;
    checks: number;
  };
  startScan: () => Promise<void>;
  isScanning: boolean;
  isInitialized: boolean;
  openSettings: (type: 'usb' | 'apps' | 'security') => Promise<void>;
}

const STORAGE_KEYS = {
  PROTECTION_ENABLED: '@guardian/protection_enabled',
  BLACKLIST: '@guardian/blacklist',
  LOGS: '@guardian/logs',
  STATS: '@guardian/stats',
  USB_STATUS: '@guardian/usb_status',
};

const GuardianContext = createContext<GuardianContextType | undefined>(undefined);

// Safe JSON parse with fallback
const safeJSONParse = <T>(value: string | null, fallback: T): T => {
  if (!value) return fallback;
  try {
    return JSON.parse(value) as T;
  } catch {
    return fallback;
  }
};

export function GuardianProvider({ children }: { children: React.ReactNode }) {
  const [isInitialized, setIsInitialized] = useState(false);
  const [isProtectionEnabled, setIsProtectionEnabled] = useState(true);
  const [blacklist, setBlacklist] = useState<BlacklistedApp[]>([]);
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [usbStatus, setUsbStatus] = useState(false);
  const [isScanning, setIsScanning] = useState(false);
  const [stats, setStats] = useState({
    threats: 0,
    blocks: 0,
    checks: 0,
  });

  const notificationListener = useRef<Notifications.Subscription | null>(null);
  const responseListener = useRef<Notifications.Subscription | null>(null);
  const isProtectionEnabledRef = useRef(isProtectionEnabled);
  
  // Keep ref updated
  useEffect(() => {
    isProtectionEnabledRef.current = isProtectionEnabled;
  }, [isProtectionEnabled]);

  useEffect(() => {
    const init = async () => {
      try {
        await loadData();
      } catch (e) {
        console.warn("Init failed:", e);
      } finally {
        setIsInitialized(true);
      }
    };
    init();

    if (Platform.OS !== 'web') {
      setupNotifications();
      notificationListener.current = Notifications.addNotificationReceivedListener(() => {});
      responseListener.current = Notifications.addNotificationResponseReceivedListener(() => {});
    }

    const interval = setInterval(() => {
      // Use ref to get current value
      if (isProtectionEnabledRef.current) {
        // Background task simulation
      }
    }, 30000);

    return () => {
      clearInterval(interval);
      notificationListener.current?.remove();
      responseListener.current?.remove();
    };
  }, []);

  const setupNotifications = async () => {
    try {
      const { status: existingStatus } = await Notifications.getPermissionsAsync();
      if (existingStatus !== 'granted') {
        await Notifications.requestPermissionsAsync();
      }
    } catch (e) {
      console.warn('Notifications setup failed', e);
    }
  };

  const loadData = async () => {
    try {
      const [enabled, storedBlacklist, storedLogs, storedStats, storedUsb] = await Promise.all([
        AsyncStorage.getItem(STORAGE_KEYS.PROTECTION_ENABLED),
        AsyncStorage.getItem(STORAGE_KEYS.BLACKLIST),
        AsyncStorage.getItem(STORAGE_KEYS.LOGS),
        AsyncStorage.getItem(STORAGE_KEYS.STATS),
        AsyncStorage.getItem(STORAGE_KEYS.USB_STATUS),
      ]);

      setIsProtectionEnabled(safeJSONParse(enabled, true));
      setBlacklist(safeJSONParse<BlacklistedApp[]>(storedBlacklist, []));
      setLogs(safeJSONParse<LogEntry[]>(storedLogs, []));
      setStats(safeJSONParse(storedStats, { threats: 0, blocks: 0, checks: 0 }));
      setUsbStatus(safeJSONParse(storedUsb, false));
    } catch (e) {
      console.error('Load data failed', e);
    }
  };

  const addLog = useCallback(async (entry: Omit<LogEntry, 'id' | 'time'>) => {
    const newEntry: LogEntry = {
      ...entry,
      id: Date.now().toString(),
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    };
    setLogs(prev => {
      const updated = [newEntry, ...prev].slice(0, 50);
      // Fire and forget - don't await
      AsyncStorage.setItem(STORAGE_KEYS.LOGS, JSON.stringify(updated)).catch(console.warn);
      return updated;
    });
  }, []);

  const sendAlert = async (title: string, body: string) => {
    if (Platform.OS === 'web') return;
    try {
      await Notifications.scheduleNotificationAsync({
        content: { title, body },
        trigger: null,
      });
    } catch (e) {
      console.warn('Alert failed', e);
    }
  };

  const toggleProtection = useCallback(async () => {
    const newValue = !isProtectionEnabled;
    setIsProtectionEnabled(newValue);
    
    try {
      await AsyncStorage.setItem(STORAGE_KEYS.PROTECTION_ENABLED, JSON.stringify(newValue));
    } catch (e) {
      console.warn('Failed to save protection state', e);
    }
    
    addLog({
      type: 'check',
      title: newValue ? 'Защита включена' : 'Защита выключена',
      desc: newValue ? 'Активный мониторинг запущен' : 'Устройство уязвимо',
    });

    if (!newValue) {
      sendAlert('Внимание!', 'Защита Guardian отключена.');
    }
  }, [isProtectionEnabled, addLog]);

  const addToBlacklist = useCallback(async (app: Omit<BlacklistedApp, 'id'>) => {
    const newApp = { ...app, id: Date.now().toString() };
    setBlacklist(prev => {
      const updated = [...prev, newApp];
      AsyncStorage.setItem(STORAGE_KEYS.BLACKLIST, JSON.stringify(updated)).catch(console.warn);
      return updated;
    });
    addLog({ type: 'block', title: 'Приложение заблокировано', desc: app.name });
  }, [addLog]);

  const removeFromBlacklist = useCallback(async (id: string) => {
    setBlacklist(prev => {
      const updated = prev.filter(a => a.id !== id);
      AsyncStorage.setItem(STORAGE_KEYS.BLACKLIST, JSON.stringify(updated)).catch(console.warn);
      return updated;
    });
  }, []);

  const toggleUsbStatus = useCallback(async () => {
    setUsbStatus(prev => {
      const newStatus = !prev;
      AsyncStorage.setItem(STORAGE_KEYS.USB_STATUS, JSON.stringify(newStatus)).catch(console.warn);
      
      if (newStatus) {
        addLog({ type: 'threat', title: 'USB отладка включена', desc: 'Обнаружена угроза' });
        sendAlert('Опасность!', 'USB-отладка включена.');
      }
      
      return newStatus;
    });
  }, [addLog]);

  const startScan = useCallback(async () => {
    if (isScanning) return;
    setIsScanning(true);
    
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    setStats(prev => {
      const newStats = {
        checks: prev.checks + 1,
        threats: usbStatus ? prev.threats + 1 : prev.threats,
        blocks: prev.blocks,
      };
      AsyncStorage.setItem(STORAGE_KEYS.STATS, JSON.stringify(newStats)).catch(console.warn);
      return newStats;
    });
    
    addLog({ type: 'check', title: 'Сканирование завершено', desc: 'Статус обновлен' });
    setIsScanning(false);
  }, [isScanning, usbStatus, addLog]);

  const openSettings = useCallback(async (type: 'usb' | 'apps' | 'security') => {
    if (Platform.OS !== 'android') {
      // Use console.warn instead of alert for web compatibility
      console.warn('Эта функция доступна только на Android устройствах');
      return;
    }

    try {
      if (type === 'usb') {
        await IntentLauncher.startActivityAsync(IntentLauncher.ActivityAction.DEVELOPER_SETTINGS);
      } else if (type === 'apps') {
        await IntentLauncher.startActivityAsync(IntentLauncher.ActivityAction.APPLICATION_SETTINGS);
      } else if (type === 'security') {
        await IntentLauncher.startActivityAsync(IntentLauncher.ActivityAction.SECURITY_SETTINGS);
      }
    } catch (e) {
      console.error('Failed to open settings', e);
    }
  }, []);

  const contextValue = useMemo(() => ({
    isProtectionEnabled,
    toggleProtection,
    blacklist,
    addToBlacklist,
    removeFromBlacklist,
    logs,
    usbStatus,
    toggleUsbStatus,
    stats,
    startScan,
    isScanning,
    isInitialized,
    openSettings,
  }), [
    isProtectionEnabled,
    toggleProtection,
    blacklist,
    addToBlacklist,
    removeFromBlacklist,
    logs,
    usbStatus,
    toggleUsbStatus,
    stats,
    startScan,
    isScanning,
    isInitialized,
    openSettings,
  ]);

  return (
    <GuardianContext.Provider value={contextValue}>
      {children}
    </GuardianContext.Provider>
  );
}

export const useGuardian = () => {
  const context = useContext(GuardianContext);
  if (!context) throw new Error('useGuardian must be used within GuardianProvider');
  return context;
};
