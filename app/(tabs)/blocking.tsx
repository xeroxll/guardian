import React, { useState } from 'react';
import { View, Text, SafeAreaView, FlatList, Pressable, Modal, TextInput, KeyboardAvoidingView, Platform } from 'react-native';
import { MaterialCommunityIcons, Ionicons } from '@expo/vector-icons';
import { useGuardian } from '@/context/GuardianContext';

export default function BlockingScreen() {
  const { blacklist, addToBlacklist, removeFromBlacklist } = useGuardian();
  const [modalVisible, setModalVisible] = useState(false);
  const [appName, setAppName] = useState('');
  const [packageName, setPackageName] = useState('');

  const handleAddApp = () => {
    if (appName.trim() && packageName.trim()) {
      addToBlacklist({ name: appName.trim(), packageName: packageName.trim() });
      setAppName('');
      setPackageName('');
      setModalVisible(false);
    }
  };

  return (
    <SafeAreaView className="flex-1 bg-[#0a0b10]">
      <View className="px-6 py-4">
        <Text className="text-2xl font-bold text-white">Чёрный список</Text>
        <Text className="text-sm text-gray-400">Управление заблокированными приложениями</Text>
      </View>

      <FlatList
        data={blacklist}
        contentContainerStyle={{ padding: 24 }}
        keyExtractor={(item) => item.id}
        renderItem={({ item }) => (
          <View className="mb-4 flex-row items-center rounded-3xl bg-[#12141c] p-4">
            <View className="mr-4 h-12 w-12 items-center justify-center rounded-2xl bg-[#1a1c26]">
              <MaterialCommunityIcons name="application" size={24} color="#94a3b8" />
            </View>
            <View className="flex-1">
              <Text className="text-base font-bold text-white">{item.name}</Text>
              <Text className="text-xs text-gray-500">{item.packageName}</Text>
            </View>
            <Pressable 
              onPress={() => removeFromBlacklist(item.id)}
              className="h-10 w-10 items-center justify-center rounded-full bg-[#2a1a1a]"
            >
              <MaterialCommunityIcons name="delete" size={20} color="#ef4444" />
            </Pressable>
          </View>
        )}
        ListEmptyComponent={
          <View className="items-center justify-center py-20">
            <MaterialCommunityIcons name="shield-off" size={64} color="#1a1c26" />
            <Text className="mt-4 text-gray-500">Список пуст</Text>
          </View>
        }
      />

      {/* Custom Modal for adding apps */}
      <Modal
        visible={modalVisible}
        animationType="slide"
        transparent={true}
        onRequestClose={() => setModalVisible(false)}
      >
        <KeyboardAvoidingView 
          className="flex-1 justify-end"
          behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        >
          <View className="rounded-t-3xl bg-[#1a1c26] p-6">
            <Text className="mb-6 text-xl font-bold text-white">Добавить приложение</Text>
            
            <Text className="mb-2 text-sm text-gray-400">Название</Text>
            <TextInput
              className="mb-4 rounded-xl bg-[#0a0b10] p-4 text-white"
              placeholder="Telegram"
              placeholderTextColor="#6b7280"
              value={appName}
              onChangeText={setAppName}
            />
            
            <Text className="mb-2 text-sm text-gray-400">Пакет (package name)</Text>
            <TextInput
              className="mb-6 rounded-xl bg-[#0a0b10] p-4 text-white"
              placeholder="org.telegram.messenger"
              placeholderTextColor="#6b7280"
              value={packageName}
              onChangeText={setPackageName}
            />
            
            <View className="flex-row gap-3">
              <Pressable 
                onPress={() => setModalVisible(false)}
                className="flex-1 rounded-xl bg-[#2a2a2a] p-4"
              >
                <Text className="text-center font-bold text-gray-400">Отмена</Text>
              </Pressable>
              <Pressable 
                onPress={handleAddApp}
                className="flex-1 rounded-xl bg-[#6366f1] p-4"
              >
                <Text className="text-center font-bold text-white">Добавить</Text>
              </Pressable>
            </View>
          </View>
        </KeyboardAvoidingView>
      </Modal>

      <Pressable 
        onPress={() => setModalVisible(true)}
        className="absolute bottom-10 right-6 h-16 w-16 items-center justify-center rounded-full bg-[#6366f1] shadow-lg"
      >
        <Ionicons name="add" size={32} color="white" />
      </Pressable>
    </SafeAreaView>
  );
}
