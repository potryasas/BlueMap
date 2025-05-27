к@echo off
echo Setting up Minecraft World Renderer...

echo Installing dependencies...
npm install

echo Creating directories...
mkdir src\textures\blocks 2>nul

echo Building project...
npm run build

echo Setup complete!
echo Please copy your Minecraft block textures to src\textures\blocks\
echo Then run 'npm start' to start the server
pause 