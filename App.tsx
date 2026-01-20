import React, { useEffect, useState } from 'react';
import {
    View,
    Text,
    Button,
    NativeModules,
    DeviceEventEmitter,
    StyleSheet,
    Alert,
    LogBox,
    StatusBar,
    AppState
} from 'react-native';

import {
    SafeAreaView
} from 'react-native-safe-area-context';

LogBox.ignoreAllLogs(true);
const { AppDetector } = NativeModules;


export default function App() {

    const [lastApp, setLastApp] = useState('-');
    const [overlayVisible, setOverlayVisible] = useState(false);
    const [overlayPermission, setOverlayPermission] = useState(false);
    const [accessibilityEnabled, setAccessibilityEnabled] = useState(false);
    const [appState, setAppState] = useState<string>(AppState.currentState);

    useEffect(() => {
        const subscription = DeviceEventEmitter.addListener(
            'onAppChanged',
            (packageName: string) => setLastApp(packageName)
        );

        // Subscribe to app state changes (foreground/background)
        const appStateListener = AppState.addEventListener('change', handleAppStateChange);

        AppDetector.getLastDetectedApp().then((pkg: string) => {
            if (pkg) setLastApp(pkg);
        });

        // Check permissions
        checkAccessibility();
        checkOverlayPermission();

        return () => {
            subscription.remove();
            appStateListener.remove();
        };
    }, []);

    const handleAppStateChange = (nextAppState: string) => {
        const previous = appState || 'unknown';
        if ((previous === 'background' || previous === 'inactive' || previous === 'active' || previous === null)
            && nextAppState === 'active') {
            checkAccessibility();
        }
        setAppState(nextAppState);
    };

    const checkAccessibility = async () => {
        const enabled = await AppDetector.isAccessibilityServiceEnabled();
        setAccessibilityEnabled(enabled);
    };


    const checkOverlayPermission = async () => {
        try {
            const granted = await AppDetector.hasOverlayPermission();
            setOverlayPermission(granted);
        } catch (e) {
            console.warn('Error checking overlay permission:', e);
        }
    };


    const handleOverlayToggle = async () => {
        try {
            // Check if overlay permission is granted
            const permissionGranted = await AppDetector.hasOverlayPermission();
            if (!permissionGranted) {
                Alert.alert(
                    'Overlay Permission',
                    'Please enable overlay permission to show the overlay.',
                    [
                        {
                            text: 'Cancel',
                            style: 'cancel',
                        },
                        {
                            text: 'Enable',
                            onPress: async () => {
                                AppDetector.openOverlayPermission();
                                setTimeout(checkOverlayPermission, 2000);
                            },
                        },
                    ]
                );
                return;
            }

            AppDetector.toggleOverlay(!overlayVisible);
            setOverlayVisible(!overlayVisible);
        } catch (e) {
            console.warn('Overlay toggle error:', e);
        }
    };

    return (
        <SafeAreaView style={styles.container}>
            <StatusBar barStyle={"dark-content"} />

            <Text style={styles.title}>App Detector</Text>

            <View style={styles.button}>
                <Button
                    title={accessibilityEnabled ? "Accessibility Service Enabled" : "Enable Accessibility Service"}
                    onPress={() => AppDetector.openAccessibilitySettings()}
                />
                {!accessibilityEnabled && (
                    <Text style={styles.description}>
                        Enabling Accessibility Service allows the app to detect which app is currently in the foreground.
                    </Text>
                )}
            </View>

            <View style={styles.button}>
                <Button
                    title={!overlayPermission ? 'Enable Overlay Permission' : overlayVisible ? 'Hide Overlay' : 'Show Overlay'}
                    onPress={handleOverlayToggle}
                />
                {!overlayPermission && (
                    <Text style={styles.description}>
                        Overlay Permission is required to display the floating overlay on top of other apps.
                    </Text>
                )}
            </View>

            <Text style={styles.log}>
                Last detected app:
                {'\n'}
                {lastApp}
            </Text>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    container: { flex: 1, padding: 24, justifyContent: 'center' },
    title: { fontSize: 24, fontWeight: 'bold', marginBottom: 20 },
    log: { marginTop: 20, fontSize: 16 },
    button: { marginBottom: 12 },
    description: { fontSize: 12, color: '#555', marginTop: 4, marginLeft: 4 },
});
