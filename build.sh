#!/bin/bash

# Build script for the E-Commerce Project

# Create necessary directories if they don't exist
mkdir -p dist
mkdir -p dist/data

# Clean previous build if it exists
rm -rf dist/*.class dist/Models dist/GUI dist/Store dist/Interfaces

# Compile all Java files and output to dist directory
echo "Compiling Java files..."
javac -d dist Main.java Models/*.java GUI/*.java Store/*.java Interfaces/*.java

# Copy data files to dist directory
echo "Copying data files..."
cp data/* dist/data/

echo "Build completed! All class files are in the dist folder."
echo "To run the application, use: cd dist && java Main"

cd dist
java Main