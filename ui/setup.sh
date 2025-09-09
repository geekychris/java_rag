#!/bin/bash

# RAG UI Setup Script
echo "🚀 Setting up RAG Search Interface..."
echo

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 16+ from https://nodejs.org/"
    exit 1
fi

# Check Node.js version
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 16 ]; then
    echo "❌ Node.js version 16+ is required. Current version: $(node -v)"
    echo "Please update Node.js from https://nodejs.org/"
    exit 1
fi

echo "✅ Node.js $(node -v) detected"

# Install dependencies
echo "📦 Installing dependencies..."
npm install

if [ $? -eq 0 ]; then
    echo "✅ Dependencies installed successfully"
else
    echo "❌ Failed to install dependencies"
    exit 1
fi

echo
echo "🎉 Setup complete! You can now start the development server:"
echo
echo "   npm start"
echo
echo "📖 The app will open at http://localhost:3000"
echo "🔧 Make sure your RAG backend is running at http://localhost:8080"
echo
echo "💡 Quick tips:"
echo "   - Edit .env to change the backend URL"
echo "   - Check README.md for detailed usage instructions"
echo "   - Use browser dev tools to debug API calls"
echo
