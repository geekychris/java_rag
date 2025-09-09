# RAG Search Interface

A modern React UI for interacting with your RAG (Retrieval-Augmented Generation) service. This interface provides an intuitive way to search your knowledge base and get AI-powered summaries.

![RAG Interface Screenshot](screenshot.png)

## Features

- **Dual Mode Operation**: Switch between search and summarization modes
- **Index Selection**: Choose from available knowledge bases
- **Advanced Search Options**: Configure result limits, similarity thresholds, and search types
- **Vector Hiding**: Toggle visibility of embedding vectors in search results
- **Real-time Health Monitoring**: See service status at a glance
- **Responsive Design**: Works on desktop, tablet, and mobile devices
- **Modern UI**: Clean, professional interface with smooth animations

## Quick Start

### Prerequisites

- Node.js 16+ and npm/yarn
- RAG backend service running (default: http://localhost:8080)

### Installation

1. Install dependencies:
```bash
npm install
```

2. Configure the backend URL (optional):
Edit `.env` file to point to your RAG service:
```env
REACT_APP_API_URL=http://localhost:8080
```

3. Start the development server:
```bash
npm start
```

4. Open your browser to [http://localhost:3000](http://localhost:3000)

## Usage Guide

### Search Mode
- Enter a natural language query
- Select your knowledge base from the dropdown
- Choose between Vector or Hybrid search
- Adjust advanced settings if needed
- View detailed results with metadata
- Toggle vector embeddings visibility

### Summarization Mode
- Enter your question or topic
- Optionally provide a custom prompt for focused summaries
- Get AI-generated summaries with source references
- View performance metrics and supporting documents

### Advanced Settings
- **Max Results**: Number of documents to retrieve (1-50)
- **Min Score**: Similarity threshold (0.0-1.0)
- **Search Type**: Vector or Hybrid search algorithms
- **Custom Prompt**: Guide the AI summary generation

## API Integration

The interface integrates with the following RAG service endpoints:

- `GET /actuator/health` - Service health check
- `POST /api/rag/search` - Document search
- `POST /api/rag/search/hybrid` - Hybrid search
- `POST /api/rag/summarize-query` - Simple query summarization
- `POST /api/rag/semantic-summarize` - Advanced semantic summarization

## Development

### Project Structure
```
src/
├── components/
│   ├── RagInterface.js       # Main interface component
│   ├── SearchResults.js      # Search results display
│   └── Summarization.js      # Summary display
├── services/
│   └── ragApi.js             # API service layer
├── index.css                 # Global styles
├── App.js                    # Root component
└── index.js                  # Entry point
```

### Building for Production

```bash
npm run build
```

This creates an optimized production build in the `build/` folder.

### Customization

#### Styling
The app uses Tailwind CSS for styling. You can customize:
- Colors in `tailwind.config.js`
- Global styles in `src/index.css`
- Component-specific styles in individual components

#### API Configuration
Update `src/services/ragApi.js` to:
- Add new endpoints
- Modify request/response handling
- Change timeout settings
- Add authentication headers

## Troubleshooting

### Common Issues

1. **Service Offline**: Check if your RAG backend is running and accessible
2. **CORS Errors**: Ensure your backend allows requests from the React dev server
3. **Empty Index List**: The interface falls back to default indexes if the backend doesn't provide them
4. **Slow Responses**: Check network connectivity and backend performance

### Debug Mode

Open browser developer tools and check the Console tab for detailed API logs and error messages.

## Browser Compatibility

- Chrome/Edge 88+
- Firefox 85+  
- Safari 14+

## License

MIT License - see LICENSE file for details

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## Support

For issues and questions:
1. Check the browser console for error messages
2. Verify your RAG service is running and accessible
3. Review the API documentation
4. Check network connectivity

---

**Happy searching!** 🔍✨
