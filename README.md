# Peer Store

A location-based marketplace Android application that allows users to buy and sell items with AI-powered image recognition and real-time chat functionality.

## Features

### 🔐 Authentication
- User registration and login
- Email-based authentication with Firebase Auth
- Password reset functionality
- Secure user session management

### 📱 Core Functionality
- **Post Creation**: Users can create listings with photos, descriptions, and prices
- **AI Image Recognition**: Automatic object detection and classification using Google ML Kit
- **Location-Based Discovery**: Find items near your location using GPS
- **Real-time Chat**: Communicate with sellers through post-specific chat rooms
- **User Profiles**: Manage your own posts and account information

### 🎯 Main Screens
- **Explore**: Browse all available posts from other users with distance calculations
- **Chat**: View and participate in conversations for all posts
- **Profile**: Manage your posts and account settings
- **Post Details**: View detailed information about items including AI descriptions

## Tech Stack

### Frontend
- **Language**: Kotlin
- **UI Framework**: Android Views with ViewBinding
- **Architecture**: Activity-based with Firebase integration

### Backend & Services
- **Authentication**: Firebase Auth
- **Database**: Cloud Firestore
- **Storage**: Firebase Storage for images
- **ML/AI**: Google ML Kit for object detection

### Key Dependencies
- Firebase SDK (Auth, Firestore, Storage)
- Google Play Services (Location, Maps)
- ML Kit Object Detection
- Picasso for image loading
- CircleImageView for profile pictures

## Project Structure

```
app/src/main/java/com/example/peerstore/
├── MainActivity.kt              # Splash screen
├── SingInActivity.kt           # User login
├── SingUpActivity.kt           # User registration  
├── ForgotPasswordActivity.kt   # Password reset
├── ExploreActivity.kt          # Browse posts
├── ChatActivity.kt             # View all chats
├── PostChatActivity.kt         # Individual post chat
├── ProfileActivity.kt          # User profile
├── CreatePostActivity.kt       # Create new post
├── PostInfoActivity.kt         # Post details
└── PostChatActivity.kt         # Chat for specific post
```

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 24+ 
- Google Services JSON configuration file

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd peer-store
   ```

2. **Firebase Setup**
   - Create a new Firebase project at [console.firebase.google.com](https://console.firebase.google.com)
   - Enable Authentication (Email/Password)
   - Enable Cloud Firestore
   - Enable Firebase Storage
   - Download `google-services.json` and place it in `app/`

3. **Configure Permissions**
   The app requires the following permissions:
   - `INTERNET` - For Firebase connectivity
   - `ACCESS_FINE_LOCATION` - For location-based features
   - `ACCESS_COARSE_LOCATION` - For location services
   - `CAMERA` - For taking photos of items

4. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ```

## Features in Detail

### AI-Powered Image Recognition
When users create posts, the app automatically analyzes uploaded photos using Google ML Kit to:
- Detect objects in images
- Generate confidence scores for identified items
- Provide automatic descriptions to supplement user descriptions

### Location Services
- Calculate distances between user location and posted items
- Display items sorted by proximity
- GPS integration for accurate positioning

### Real-time Chat System
- Post-specific chat rooms stored in Firestore
- Real-time message synchronization
- User identification in chat messages

### User Experience
- Clean, Material Design-inspired interface
- Bottom navigation for easy screen switching
- Image rotation handling for camera captures
- Responsive layouts for different screen sizes

## Database Schema

### Users Collection
```json
{
  "posts": ["postId1", "postId2", ...]
}
```

### Posts Collection
```json
{
  "title": "Item title",
  "image": "https://firebase-storage-url",
  "imageName": "unique-image-name.jpg", 
  "descriptionAI": "AI-generated description",
  "descriptionOwner": "User description",
  "price": "100",
  "owner": "userId",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "globalChat": ["userId_message1", "userId_message2", ...]
}
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Create a Pull Request

## Security Considerations

- User authentication handled securely through Firebase Auth
- Database rules should be configured to ensure users can only modify their own data
- Image uploads are processed through Firebase Storage with proper access controls
- Location data is used only for distance calculations and not stored permanently

## Future Enhancements

- [ ] Push notifications for new messages
- [ ] Advanced search and filtering options
- [ ] User ratings and reviews system
- [ ] Payment integration
- [ ] Enhanced AI descriptions with more detailed analysis
- [ ] Dark mode support
- [ ] Multi-language support

## License

This project is available under the MIT License. See LICENSE file for more details.

## Support

For issues and questions, please create an issue in the repository or contact the development team.
