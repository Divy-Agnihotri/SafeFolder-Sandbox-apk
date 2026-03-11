# SafeFolder 🔐

**SafeFolder** is an Android application designed to securely hide and protect files on your device using Android's built-in **sandboxing** and **scoped storage** mechanisms.

The app ensures that sensitive files remain isolated from other apps, improving privacy and security without requiring root access.

---

## ✨ Features

* 🔒 **Secure File Storage**
  Files stored in SafeFolder are protected using Android's application sandbox.

* 📂 **Scoped Storage Support**
  Uses Android's modern scoped storage system to prevent other apps from accessing protected files.

* 🛡 **App-Level Isolation**
  Files are kept inside the app's private directory so that other applications cannot read them.

* 🚫 **No Root Required**
  Works on standard Android devices without special permissions.

* ⚡ **Lightweight and Fast**
  Minimal UI and efficient storage handling.

---

## 🧠 How It Works

SafeFolder relies on two core Android security features:

### 1. App Sandboxing

Each Android application runs in its own sandbox with a unique Linux user ID. This means:

* Other apps cannot access SafeFolder’s internal files.
* Files stored in the app's private storage remain isolated.

### 2. Scoped Storage

Scoped storage restricts how apps access shared storage on the device.

SafeFolder uses scoped storage to:

* Control file access
* Prevent other apps from browsing protected files
* Maintain secure file management

---

## 📱 Requirements

* Android 10 (API 29) or higher
* Storage access permission (when importing files)

---

## 🛠 Installation

1. Clone the repository:

```bash
git clone https://github.com/yourusername/safefolder.git
```

2. Open the project in **Android Studio**.

3. Build and run the application on your device or emulator.

---

## 🔐 Security Notes

* Files stored in SafeFolder are placed in the app’s **internal storage directory**.
* Android prevents other apps from accessing this directory.
* Uninstalling the app will permanently delete stored files.

---

## 🚧 Future Improvements

* PIN / biometric authentication
* File encryption
* Hidden gallery mode
* Backup & restore support

---

## 🤝 Contributing

Contributions are welcome!

1. Fork the repository
2. Create a new branch
3. Submit a pull request

---

## 📄 License

This project is licensed under the **MIT License**.
