#!/bin/bash

# Compile the Java files
echo "Compiling Java files..."
javac -cp "lib/*" src/database/*.java src/doodle_server/*.java src/model/*.java -d bin/

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful."
    # Run the application
    echo "Running the application..."
    java -cp "bin:lib/*" doodle_server.Server
else
    echo "Compilation failed."
fi

