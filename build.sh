#!/bin/bash

# Cross-platform E-Commerce Project Setup and Execution Script
# This script handles dependency checks, Docker installation/setup, 
# database setup, compilation, and running the application
# Simply run this script and it will take care of everything

# Colors for output (UNIX systems)
if [[ "$OSTYPE" != "win"* ]]; then
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  YELLOW='\033[1;33m'
  BLUE='\033[0;34m'
  NC='\033[0m' # No Color
else
  GREEN=''
  RED=''
  YELLOW=''
  BLUE=''
  NC=''
fi

# Trap ctrl-c and call cleanup function
trap cleanup INT

# Cleanup function to stop Docker services when script is interrupted
function cleanup {
    echo -e "\n${YELLOW}Shutting down the application and stopping Docker services...${NC}"
    
    # Use our more robust stop_docker_services function for cleanup
    stop_docker_services
    
    exit 0
}

# Function to stop Docker services called when application exits
function stop_docker_services {
    echo -e "${YELLOW}Stopping Docker services...${NC}"
    
    # First check if the container is running
    if (docker ps | grep -q ecommerce_db) || (sudo docker ps | grep -q ecommerce_db); then
        # Try to stop Docker containers with increasing privileges
        docker-compose down -v 2>/dev/null || \
        docker compose down -v 2>/dev/null || \
        sudo docker-compose down -v 2>/dev/null || \
        sudo docker compose down -v
        
        # Extra step: Force remove the container if it's still around
        echo -e "${YELLOW}Ensuring all containers are fully stopped...${NC}"
        docker rm -f ecommerce_db 2>/dev/null || \
        sudo docker rm -f ecommerce_db 2>/dev/null
        
        # Verify the container is stopped
        sleep 2
        if (docker ps | grep -q ecommerce_db) || (sudo docker ps | grep -q ecommerce_db); then
            echo -e "${RED}Warning: Container is still running. Trying again with force...${NC}"
            docker kill ecommerce_db 2>/dev/null || \
            sudo docker kill ecommerce_db 2>/dev/null
            
            docker rm -f ecommerce_db 2>/dev/null || \
            sudo docker rm -f ecommerce_db 2>/dev/null
        fi
        
        # Final verification
        if ! (docker ps | grep -q ecommerce_db) && ! (sudo docker ps | grep -q ecommerce_db); then
            echo -e "${GREEN}Docker containers successfully stopped.${NC}"
        else
            echo -e "${RED}Warning: Unable to completely stop all containers.${NC}"
            echo -e "${RED}You may need to run 'docker kill ecommerce_db' manually.${NC}"
        fi
    else
        echo -e "${GREEN}No running containers found.${NC}"
    fi
}

# Detect OS
detect_os() {
  case "$OSTYPE" in
    darwin*)  echo "macos" ;; 
    linux*)   echo "linux" ;;
    msys*|cygwin*|mingw*) echo "windows" ;;
    *)        echo "unknown" ;;
  esac
}

OS=$(detect_os)
echo "Detected operating system: $OS"

# Banner
function show_banner {
    echo -e "${BLUE}"
    echo "========================================================"
    echo "   E-Commerce Application - Complete Setup & Run Tool   "
    echo "========================================================"
    echo -e "${NC}"
    echo "This script will:"
    echo " 1. Check for required dependencies (Java, MySQL connector)"
    echo " 2. Check and install Docker if needed"
    echo " 3. Set up the database container"
    echo " 4. Compile the Java application"
    echo " 5. Run the application"
    echo ""
    echo "You don't need to know anything about the project to use it!"
    if [[ "$OS" != "windows" ]]; then
      echo -e "${YELLOW}NOTE: You might be asked for sudo password for Docker operations${NC}"
    fi
    echo ""
}

# Print usage information
function print_usage {
    echo -e "${YELLOW}Usage: ./build.sh [OPTIONS]${NC}"
    echo "Options:"
    echo "  --clean         Clean previous build artifacts"
    echo "  --build-only    Only compile the application without running it"
    echo "  --run-only      Run the application without recompiling"
    echo "  --docker-reset  Restart the Docker container from scratch (WARNING: resets database)"
    echo "  --help          Show this help message"
    echo ""
}

# Check if MySQL connector exists
function check_mysql_connector {
    echo -e "${YELLOW}Checking for MySQL connector...${NC}"
    
    if [ ! -f "lib/mysql-connector-j-8.0.33.jar" ]; then
        echo -e "${RED}MySQL connector not found in lib directory.${NC}"
        echo -e "${YELLOW}Downloading MySQL connector...${NC}"
        
        mkdir -p lib
        
        if command -v curl &> /dev/null; then
            curl -L "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar" -o lib/mysql-connector-j-8.0.33.jar
        elif command -v wget &> /dev/null; then
            wget "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar" -O lib/mysql-connector-j-8.0.33.jar
        else
            echo -e "${RED}Neither curl nor wget is installed. Cannot download MySQL connector.${NC}"
            echo "Please download MySQL connector manually from https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar"
            echo "and place it in the lib directory."
            exit 1
        fi
        
        if [ -f "lib/mysql-connector-j-8.0.33.jar" ]; then
            echo -e "${GREEN}MySQL connector downloaded successfully.${NC}"
        else
            echo -e "${RED}Failed to download MySQL connector.${NC}"
            exit 1
        fi
    else
        echo -e "${GREEN}MySQL connector found in lib directory.${NC}"
    fi
}

# Function to check if Docker is installed and install if not
function check_and_install_docker {
    echo -e "${YELLOW}Checking if Docker is installed...${NC}"
    
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}Docker is not installed.${NC}"
        
        case "$OS" in
            linux)
                echo -e "${YELLOW}Installing Docker for Linux...${NC}"
                
                # Check Linux distribution
                if [ -f /etc/os-release ]; then
                    . /etc/os-release
                    DISTRO=$NAME
                else
                    DISTRO="Unknown"
                fi
                
                # Install Docker based on the distribution
                case "$DISTRO" in
                    *"Ubuntu"*|*"Debian"*)
                        echo "Detected Ubuntu/Debian-based system"
                        sudo apt-get update
                        sudo apt-get install -y apt-transport-https ca-certificates curl software-properties-common
                        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
                        sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
                        sudo apt-get update
                        sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
                        ;;
                    *"Fedora"*|*"CentOS"*|*"Red Hat"*)
                        echo "Detected Fedora/CentOS/RHEL system"
                        sudo dnf -y install dnf-plugins-core
                        sudo dnf config-manager --add-repo https://download.docker.com/linux/fedora/docker-ce.repo
                        sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
                        ;;
                    *"Arch"*)
                        echo "Detected Arch Linux"
                        sudo pacman -Syu --noconfirm docker docker-compose
                        ;;
                    *)
                        echo -e "${RED}Unsupported Linux distribution. Please install Docker manually.${NC}"
                        echo "Visit https://docs.docker.com/engine/install/ for instructions."
                        exit 1
                        ;;
                esac
                
                # Start and enable Docker service
                sudo systemctl start docker
                sudo systemctl enable docker
                
                # Add current user to docker group to avoid using sudo
                sudo usermod -aG docker $USER
                echo -e "${GREEN}Docker has been installed!${NC}"
                echo -e "${YELLOW}NOTE: You might need to log out and log back in to use Docker without sudo.${NC}"
                ;;
                
            macos)
                echo -e "${YELLOW}Installing Docker for macOS...${NC}"
                
                # Check if Homebrew is installed
                if command -v brew &> /dev/null; then
                    echo -e "${YELLOW}Using Homebrew to install Docker...${NC}"
                    
                    # Install Docker using Homebrew
                    brew install --cask docker
                    
                    if [ $? -eq 0 ]; then
                        echo -e "${GREEN}Docker has been installed via Homebrew!${NC}"
                        echo -e "${YELLOW}Starting Docker Desktop...${NC}"
                        
                        # Start Docker Desktop
                        open -a Docker
                        
                        echo -e "${YELLOW}Waiting for Docker to start (this may take a minute)...${NC}"
                        # Wait for Docker to start
                        echo -e "${YELLOW}Please complete any Docker Desktop onboarding if it appears.${NC}"
                        
                        # Give user time to complete setup
                        for i in {1..30}; do
                            echo -n "."
                            sleep 1
                            # Check if Docker is running yet
                            if docker info &>/dev/null; then
                                echo ""
                                echo -e "${GREEN}Docker is now running!${NC}"
                                break
                            fi
                            # If we've waited 30 seconds and Docker isn't running, continue anyway
                            if [ $i -eq 30 ]; then
                                echo ""
                                echo -e "${YELLOW}Docker might not be fully started yet. You may need to open Docker Desktop manually.${NC}"
                            fi
                        done
                    else
                        echo -e "${RED}Failed to install Docker via Homebrew.${NC}"
                        echo -e "${YELLOW}Attempting alternative installation method...${NC}"
                        
                        # Alternative: manual download
                        echo -e "${YELLOW}Please download and install Docker Desktop manually from:${NC}"
                        echo -e "https://www.docker.com/products/docker-desktop"
                        echo -e "${YELLOW}After installation, press Enter to continue or Ctrl+C to exit...${NC}"
                        read
                    fi
                else
                    # If Homebrew is not installed, try to install it
                    echo -e "${YELLOW}Homebrew not found. Attempting to install Homebrew...${NC}"
                    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
                    
                    if command -v brew &> /dev/null; then
                        echo -e "${GREEN}Homebrew installed successfully!${NC}"
                        echo -e "${YELLOW}Now installing Docker...${NC}"
                        brew install --cask docker
                        
                        echo -e "${YELLOW}Starting Docker Desktop...${NC}"
                        open -a Docker
                        echo -e "${YELLOW}Please complete any Docker Desktop onboarding if it appears.${NC}"
                        echo -e "${YELLOW}Waiting for Docker to start...${NC}"
                        sleep 20
                    else
                        echo -e "${RED}Could not install Homebrew. Manual Docker installation required.${NC}"
                        echo -e "${YELLOW}Please download and install Docker Desktop manually from:${NC}"
                        echo -e "https://www.docker.com/products/docker-desktop"
                        echo -e "${YELLOW}After installation, press Enter to continue or Ctrl+C to exit...${NC}"
                        read
                    fi
                fi
                ;;
                
            windows)
                echo -e "${YELLOW}Docker needs to be installed manually on Windows.${NC}"
                echo "Please download and install Docker Desktop from https://www.docker.com/products/docker-desktop"
                echo "After installation, press Enter to continue or Ctrl+C to exit..."
                read
                ;;
                
            *)
                echo -e "${RED}Unsupported operating system. Please install Docker manually.${NC}"
                echo "Visit https://docs.docker.com/engine/install/ for instructions."
                exit 1
                ;;
        esac
    else
        echo -e "${GREEN}Docker is already installed.${NC}"
    fi
    
    # Check for docker-compose
    if ! command -v docker-compose &> /dev/null && ! command -v docker compose &> /dev/null; then
        echo -e "${YELLOW}Docker Compose not found. Installing...${NC}"
        
        case "$OS" in
            linux)
                sudo curl -L "https://github.com/docker/compose/releases/download/v2.18.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
                sudo chmod +x /usr/local/bin/docker-compose
                ;;
                
            macos)
                brew install docker-compose
                ;;
                
            windows)
                echo "Docker Compose is typically included with Docker Desktop for Windows."
                echo "If it's not working, please reinstall Docker Desktop."
                ;;
                
            *)
                echo -e "${RED}Cannot install Docker Compose on unknown OS.${NC}"
                exit 1
                ;;
        esac
        
        echo -e "${GREEN}Docker Compose has been installed!${NC}"
    else
        echo -e "${GREEN}Docker Compose is already installed.${NC}"
    fi
}

# Function to start Docker if not running
function ensure_docker_running {
    echo -e "${YELLOW}Checking if Docker daemon is running...${NC}"
    
    # First try without sudo
    if docker info &> /dev/null; then
        echo -e "${GREEN}Docker daemon is running.${NC}"
        return 0
    fi
    
    # If that failed, try with sudo
    if sudo docker info &> /dev/null; then
        echo -e "${GREEN}Docker daemon is running (requires sudo).${NC}"
        echo -e "${YELLOW}Note: You may need to run docker commands with sudo${NC}"
        return 0
    fi
    
    # If we get here, Docker is not running
    echo -e "${RED}Docker daemon is not running.${NC}"
    
    case "$OS" in
        linux)
            echo -e "${YELLOW}Attempting to start Docker daemon...${NC}"
            sudo systemctl start docker
            sleep 5 # Wait longer for Docker to start properly
            
            # Check again with sudo
            if sudo docker info &> /dev/null; then
                echo -e "${GREEN}Docker daemon started successfully.${NC}"
                return 0
            else
                echo -e "${RED}Failed to start Docker daemon automatically.${NC}"
                echo "Try running these commands manually:"
                echo "sudo systemctl start docker"
                echo "sudo systemctl status docker"
                echo "Then run this script again with ./build.sh"
                exit 1
            fi
            ;;
            
        macos|windows)
            echo -e "${YELLOW}Please start Docker Desktop manually.${NC}"
            echo "After starting Docker Desktop, press Enter to continue or Ctrl+C to exit..."
            read
            
            if ! docker info &> /dev/null; then
                echo -e "${RED}Docker is still not running. Please ensure Docker Desktop is running.${NC}"
                exit 1
            fi
            ;;
            
        *)
            echo -e "${RED}Cannot start Docker on unknown OS.${NC}"
            exit 1
            ;;
    esac
}

# Function to handle Docker container
function handle_docker {
    # Check if container needs to be reset
    if [[ "$1" == "reset" ]]; then
        echo -e "${YELLOW}Resetting Docker container...${NC}"
        
        # Try methods with increasing privileges
        docker-compose down -v 2>/dev/null || \
        docker compose down -v 2>/dev/null || \
        sudo docker-compose down -v 2>/dev/null || \
        sudo docker compose down -v
        
        docker-compose up -d 2>/dev/null || \
        docker compose up -d 2>/dev/null || \
        sudo docker-compose up -d 2>/dev/null || \
        sudo docker compose up -d
        
        echo -e "${GREEN}Docker container reset. Waiting for MySQL to initialize (30 seconds)...${NC}"
        sleep 30
    else
        # Check if MySQL Docker container is running
        if ! (docker ps | grep -q ecommerce_db) && ! (sudo docker ps | grep -q ecommerce_db); then
            echo -e "${YELLOW}Starting MySQL Docker container...${NC}"
            
            # Try methods with increasing privileges
            docker-compose up -d 2>/dev/null || \
            docker compose up -d 2>/dev/null || \
            sudo docker-compose up -d 2>/dev/null || \
            sudo docker compose up -d
            
            echo -e "${GREEN}Waiting for MySQL to initialize (30 seconds)...${NC}"
            sleep 30
        else
            echo -e "${GREEN}MySQL Docker container is already running.${NC}"
        fi
    fi
    
    # Verify database connection with increasing privileges
    echo "Verifying database connection..."
    if docker exec ecommerce_db mysqladmin -uecomuser -pecompass ping --silent 2>/dev/null; then
        echo -e "${GREEN}Database connection successful.${NC}"
    elif sudo docker exec ecommerce_db mysqladmin -uecomuser -pecompass ping --silent 2>/dev/null; then
        echo -e "${GREEN}Database connection successful (requires sudo).${NC}"
        echo -e "${YELLOW}Note: You may need to run docker commands with sudo${NC}"
    else
        echo -e "${RED}Error: Cannot connect to MySQL database.${NC}"
        echo "Check Docker logs with: docker logs ecommerce_db"
        exit 1
    fi
}

# Function to check and install Java if needed
function check_and_install_java {
    echo -e "${YELLOW}Checking if Java is installed...${NC}"
    
    if ! command -v java &> /dev/null || ! command -v javac &> /dev/null; then
        echo -e "${RED}Java Development Kit (JDK) is not installed.${NC}"
        
        case "$OS" in
            linux)
                echo -e "${YELLOW}Installing OpenJDK on Linux...${NC}"
                
                # Check Linux distribution
                if [ -f /etc/os-release ]; then
                    . /etc/os-release
                    DISTRO=$NAME
                else
                    DISTRO="Unknown"
                fi
                
                # Install Java based on the distribution
                case "$DISTRO" in
                    *"Ubuntu"*|*"Debian"*)
                        sudo apt-get update
                        sudo apt-get install -y openjdk-17-jdk
                        ;;
                    *"Fedora"*|*"CentOS"*|*"Red Hat"*)
                        sudo dnf install -y java-17-openjdk-devel
                        ;;
                    *"Arch"*)
                        sudo pacman -Syu --noconfirm jdk-openjdk
                        ;;
                    *)
                        echo -e "${RED}Unsupported Linux distribution. Please install Java manually.${NC}"
                        echo "You need Java Development Kit (JDK) 11 or later."
                        exit 1
                        ;;
                esac
                ;;
                
            macos)
                echo -e "${YELLOW}Installing OpenJDK on macOS...${NC}"
                if command -v brew &> /dev/null; then
                    brew install openjdk@17
                    # Create symlink to make it available to system
                    sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
                else
                    echo -e "${RED}Homebrew is not installed. Cannot install Java automatically.${NC}"
                    echo "Please install Homebrew first (https://brew.sh/) or install JDK manually."
                    echo "You need Java Development Kit (JDK) 11 or later."
                    exit 1
                fi
                ;;
                
            windows)
                echo -e "${YELLOW}Java needs to be installed manually on Windows.${NC}"
                echo "Please download and install JDK 17 from https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/"
                echo "After installation, make sure to add Java to your PATH."
                echo "Press Enter to continue or Ctrl+C to exit..."
                read
                ;;
                
            *)
                echo -e "${RED}Unsupported operating system. Please install Java manually.${NC}"
                echo "You need Java Development Kit (JDK) 11 or later."
                exit 1
                ;;
        esac
        
        echo -e "${GREEN}Java has been installed!${NC}"
    else
        echo -e "${GREEN}Java is already installed.${NC}"
    fi
    
    # Verify Java version
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo -e "${GREEN}Using Java version: $java_version${NC}"
}

# Function to clean the build
function clean_build {
    echo -e "${YELLOW}Cleaning previous build...${NC}"
    rm -rf dist
    echo -e "${GREEN}Clean completed.${NC}"
}

# Function to compile the application
function compile_app {
    echo -e "${YELLOW}Creating build directories...${NC}"
    mkdir -p dist
    mkdir -p dist/data
    mkdir -p dist/lib
    
    # Copy data files
    if [ -d "data" ]; then
        echo -e "${YELLOW}Copying data files...${NC}"
        cp -r data/* dist/data/ 2>/dev/null || :
    fi
    
    echo -e "${YELLOW}Compiling Java files...${NC}"
    
    # Handle classpath separator based on OS
    if [[ "$OS" == "windows" ]]; then
        CP_SEP=";"
    else
        CP_SEP=":"
    fi
    
    javac -d dist -cp .$CP_SEP"lib/mysql-connector-j-8.0.33.jar" Main.java Models/*.java GUI/*.java Store/*.java Interfaces/*.java utils/*.java
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Compilation failed!${NC}"
        exit 1
    fi
    
    # Copy library files
    echo -e "${YELLOW}Copying library files...${NC}"
    cp lib/mysql-connector-j-8.0.33.jar dist/lib/
    
    echo -e "${GREEN}Build completed successfully! All class files are in the dist folder.${NC}"
}

# Function to run the application
function run_app {
    echo -e "${YELLOW}Running the application...${NC}"
    
    # Handle classpath separator based on OS
    if [[ "$OS" == "windows" ]]; then
        CP_SEP=";"
    else
        CP_SEP=":"
    fi
    
    cd dist
    java -cp .$CP_SEP"lib/mysql-connector-j-8.0.33.jar" Main
    JAVA_EXIT_CODE=$?
    cd ..
    
    echo -e "${BLUE}Application exited. You can run it again with './build.sh --run-only'${NC}"
    
    # Ask user if they want to stop Docker services
    if [ "$AUTO_SHUTDOWN" == "true" ]; then
        stop_docker_services
    else
        echo -e "${YELLOW}Do you want to stop Docker services? (y/n)${NC}"
        read -r stop_docker
        if [[ "$stop_docker" =~ ^[Yy]$ ]]; then
            stop_docker_services
        else
            echo -e "${YELLOW}Docker services will continue running in the background.${NC}"
            echo -e "To stop them later, run: docker-compose down"
        fi
    fi
}

# Parse command line arguments
CLEAN=false
DO_BUILD=true
DO_RUN=true
DOCKER_RESET=false
AUTO_SHUTDOWN=false

# If no arguments, run everything
if [ $# -eq 0 ]; then
    CLEAN=true
    DO_BUILD=true
    DO_RUN=true
fi

while [[ $# -gt 0 ]]; do
    case $1 in
        --clean)
            CLEAN=true
            DO_BUILD=true
            DO_RUN=true
            shift
            ;;
        --build-only)
            CLEAN=true
            DO_BUILD=true
            DO_RUN=false
            shift
            ;;
        --run-only)
            CLEAN=false
            DO_BUILD=false
            DO_RUN=true
            shift
            ;;
        --docker-reset)
            DOCKER_RESET=true
            CLEAN=true
            DO_BUILD=true
            DO_RUN=true
            shift
            ;;
        --auto-shutdown)
            AUTO_SHUTDOWN=true
            shift
            ;;
        --help)
            print_usage
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            print_usage
            exit 1
            ;;
    esac
done

# Main execution flow
show_banner

# Step 1: Check for MySQL connector
check_mysql_connector

# Step 2: Check and install prerequisites
check_and_install_java
check_and_install_docker
ensure_docker_running

# Step 3: Handle Docker
if [[ "$DOCKER_RESET" == "true" ]]; then
    handle_docker "reset"
else
    handle_docker
fi

# Step 4: Clean if needed
if [[ "$CLEAN" == "true" ]]; then
    clean_build
fi

# Step 5: Build if needed
if [[ "$DO_BUILD" == "true" ]]; then
    compile_app
fi

# Step 6: Run if needed
if [[ "$DO_RUN" == "true" ]]; then
    run_app
fi

echo -e "${GREEN}=== E-Commerce Application script completed! ===${NC}"