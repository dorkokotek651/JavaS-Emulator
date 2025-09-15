#!/bin/bash

# S-Emulator JavaFX Application Runner
# This script builds and runs the complete S-Emulator JavaFX application

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}${BOLD}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}${BOLD}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}${BOLD}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}${BOLD}[ERROR]${NC} $1"
}

# Function to check if Java 21 is available
check_java() {
    print_status "Checking Java version..."
    
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | head -n 1 | cut -d '"' -f 2 | cut -d '.' -f 1)
    
    if [ "$java_version" -lt 21 ]; then
        print_error "Java 21 or higher is required. Found version: $java_version"
        print_warning "Please install Java 21 or set JAVA_HOME to point to Java 21"
        exit 1
    fi
    
    print_success "Java version check passed (version $java_version)"
}

# Function to check if Maven is available
check_maven() {
    print_status "Checking Maven..."
    
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed or not in PATH"
        print_warning "Please install Apache Maven"
        exit 1
    fi
    
    maven_version=$(mvn -version 2>&1 | head -n 1 | cut -d ' ' -f 3)
    print_success "Maven check passed (version $maven_version)"
}

# Function to clean and build the project
build_project() {
    print_status "Cleaning and building S-Emulator project..."
    
    # Clean all modules
    print_status "Cleaning project..."
    mvn clean -q
    
    # Install engine module first (required by fx module)
    print_status "Building and installing engine module..."
    cd s-emulator-engine
    mvn clean compile install -q -DskipTests
    cd ..
    
    # Build the JavaFX module
    print_status "Building JavaFX module..."
    cd s-emulator-fx
    mvn compile -q -DskipTests
    cd ..
    
    print_success "Project build completed successfully"
}

# Function to run tests
run_tests() {
    print_status "Running tests..."
    
    # Run engine tests
    print_status "Running engine tests..."
    cd s-emulator-engine
    mvn test -q
    cd ..
    
    print_success "All tests passed"
}

# Function to run the JavaFX application
run_application() {
    print_status "Starting S-Emulator JavaFX Application..."
    print_warning "The application window should appear shortly..."
    print_warning "Close the application window to return to the terminal"
    
    cd s-emulator-fx
    
    # Try to run with JavaFX plugin
    if mvn javafx:run; then
        print_success "Application closed successfully"
    else
        print_error "Failed to start JavaFX application"
        print_warning "Trying alternative launch method..."
        
        # Alternative: run with java command directly
        print_status "Building executable jar and running..."
        mvn package -q
        
        # Find the main class and run it
        if [ -f "target/classes/fx/SEmulatorFXApplication.class" ]; then
            java -cp "target/classes:target/dependency/*" fx.SEmulatorFXApplication
        else
            print_error "Could not find compiled application class"
            exit 1
        fi
    fi
    
    cd ..
}

# Function to show usage
show_usage() {
    echo ""
    echo -e "${BOLD}S-Emulator JavaFX Application Runner${NC}"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --with-tests  Run tests during build (tests are skipped by default)"
    echo "  --no-tests    Skip running tests (default behavior)"
    echo "  --build-only  Build the project but don't run the application"
    echo "  --run-only    Run the application without building (assumes already built)"
    echo "  --help        Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                  # Full build (no tests) and run"
    echo "  $0 --with-tests     # Build with tests and run"
    echo "  $0 --build-only     # Only build the project (no tests)"
    echo "  $0 --run-only       # Only run the application"
    echo ""
}

# Main execution
main() {
    echo ""
    echo -e "${BOLD}🚀 S-Emulator JavaFX Application Runner${NC}"
    echo -e "${BOLD}=======================================${NC}"
    echo ""
    
    # Parse command line arguments
    SKIP_TESTS=true  # Skip tests by default
    BUILD_ONLY=false
    RUN_ONLY=false
    
    for arg in "$@"; do
        case $arg in
            --with-tests)
                SKIP_TESTS=false
                ;;
            --no-tests)
                SKIP_TESTS=true
                ;;
            --build-only)
                BUILD_ONLY=true
                ;;
            --run-only)
                RUN_ONLY=true
                ;;
            --help)
                show_usage
                exit 0
                ;;
            *)
                print_error "Unknown option: $arg"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # Check prerequisites
    check_java
    check_maven
    
    if [ "$RUN_ONLY" = false ]; then
        # Build the project
        build_project
        
        # Run tests only if explicitly requested
        if [ "$SKIP_TESTS" = false ]; then
            run_tests
        else
            print_status "Skipping tests (use --with-tests to run tests)"
        fi
    fi
    
    if [ "$BUILD_ONLY" = false ]; then
        # Run the application
        run_application
    else
        print_success "Build completed. Use --run-only to start the application."
    fi
    
    echo ""
    print_success "Script completed successfully!"
    echo ""
}

# Run main function with all arguments
main "$@"
