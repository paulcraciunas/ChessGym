import pandas as pd
import sys
import os

def main():
    # Check if the file arguments are provided
    if len(sys.argv) != 3:
        print("Error: Missing file arguments.")
        print("Usage: python lichess_data_sanitizer.py <input_path> <output_path>")
        sys.exit(1)

    input_path = sys.argv[1]
    output_path = sys.argv[2]

    # Verify that the input file exists
    if not os.path.isfile(input_path):
        print(f"Error: File '{input_path}' does not exist.")
        sys.exit(1)
    # Verify that the output file does not exist
    if os.path.isfile(output_path):
        print(f"Error: File '{output_path}' already exists.")
        sys.exit(1)

    process_csv(input_path, output_path)

def process_csv(input_file, output_file):
    # Read the CSV file
    df = pd.read_csv(input_file)

    # Replace the value in the first column with the value from the 4th column
    df.iloc[:, 0] = df.iloc[:, 3]

    # Drop the fourth column
    df = df.drop(df.columns[3], axis=1)

    # Sort the DataFrame by the values in the first column
    df = df.sort_values(by=df.columns[0])

    # Save the modified DataFrame to a new CSV file
    df.to_csv(output_file, index=False)

if __name__ == "__main__":
    main()