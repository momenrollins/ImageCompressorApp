# ImageCompressorApp

## Overview
ImageCompressorApp is an Android application that compresses images to reduce their size while maintaining quality. It supports images from URLs, or shared content, and allows users to share compressed images directly.

## Features
- Compress images from a URL, or shared content.
- Customizable compression threshold for image size.
- Displays both uncompressed and compressed images.
- Enables users to download or share compressed images.

## Screenshots
<img src="https://github.com/user-attachments/assets/22722e97-e5f8-4565-a6a8-f066bc066b23" width=200><img src="https://github.com/user-attachments/assets/f4427019-98a8-4c21-921f-ff47392ee451" width=200>
<img src="https://github.com/user-attachments/assets/2bae9453-90a0-4dbf-bef9-4c013002976c" width=200><img src="https://github.com/user-attachments/assets/804c50c7-89ce-44ed-96c7-2f3883f2c5fb" width=200>
## How It Works
1. Enter an image URL or share an image with the app.
2. The app compresses the image using a WorkManager and displays the results.
3. View the original and compressed images.
4. Share or download the compressed image.

## Tech Stack
- **Language:** Kotlin
- **Architecture:** MVVM with ViewModel and LiveData
- **Libraries and Tools:**
  - WorkManager
  - Jetpack Compose
  - Coil for image loading
  - FileProvider for secure file sharing

## Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/momenrollins/ImageCompressorApp.git
   ```
2. Open the project in Android Studio.
3. Build and run the project on an Android emulator or device.

## Usage
- Launch the app and input an image URL or share an image via the Android share feature.
- Wait for the app to compress the image.
- View, download, or share the compressed image.

## File Structure
- **ImageCompressorWorker:** Handles image compression in the background.
- **ImageProcessingViewModel:** Manages UI state and business logic.
- **ImageSharingActivity:** Displays the app UI and handles user interactions.
- **res/xml/file_paths.xml:** Configures FileProvider paths for sharing compressed images.

## Contributing
Contributions are welcome! Please fork the repository and submit a pull request with your changes.

## Author
Mo'men Refaat

---
Feel free to reach out if you have any questions or suggestions!

