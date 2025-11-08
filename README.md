# CristiqBot

A Minecraft plugin that adds an AI assistant to your server using Together.ai's Gemini model. Compatible with Paper/Folia 1.20.4.

## Features

- Chat with an AI assistant using natural language
- AI adapts its personality based on the dimension (Overworld, Nether, End)
- Search for player bases and structures using AI-powered descriptions
- Rate limiting and cooldowns to prevent abuse
- Async operations for optimal performance

## Installation

1. Push this code to a GitHub repository
2. Go to the Actions tab in your repository
3. Download the built plugin from the latest successful workflow run
4. Place the downloaded `CristiqBot-1.0.0.jar` in your server's `plugins` folder
5. Start/restart your server

## Usage

### Commands

- `/cristiqbot talkai <on|off>` - Enable or disable AI chat responses
- `/cristiqbot search base "<description>"` - Search for bases matching your description

### Chat

Once enabled with `/cristiqbot talkai on`, you can chat with the AI by starting your message with "CristiqBot". For example:
```
CristiqBot what's the best way to find diamonds?
```

### Configuration

Edit `plugins/CristiqBot/config.yml` to customize:
- AI provider and model settings
- Chat behavior and personality
- Search radius and cooldowns
- Highlighted blocks for base detection

## Requirements

- Paper/Folia 1.20.4 server
- Java 17 or higher
- Together.ai API key (configured in config.yml)

## Building from Source

1. Install JDK 17 and Maven
2. Run `mvn clean package`
3. Find the built plugin in `target/CristiqBot-1.0.0.jar`
