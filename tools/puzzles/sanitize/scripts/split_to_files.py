import pandas as pd
import sys
import os

def main():
    # Check if the arguments are provided
    if len(sys.argv) != 3:
        print("Error: Missing file argument.")
        print("Usage: python script.py <file_path> <output_path>")
        sys.exit(1)

    file_path = sys.argv[1]
    output_path = sys.argv[2]

    # Verify that the file exists
    if not os.path.isfile(file_path):
        print(f"Error: File '{file_path}' does not exist.")
        sys.exit(1)

    split_csv_by_column(file_path, output_path)

def split_csv_by_column(input_file, output_path):
    # Read the CSV file
    df = pd.read_csv(input_file)

    # Get unique values from the first column
    unique_values = df.iloc[:, 0].unique()

    # Split the DataFrame and save each part to a new file
    for value in unique_values:
        # Filter rows where the first column matches the unique value
        subset = df[df.iloc[:, 0] == value]

        # Drop the first column
        subset = subset.iloc[:, 1:]

        # Save to a new file named after the unique value
        output_file = f"{output_path}/{value}.csv"
        subset.to_csv(output_file, index=False)

if __name__ == "__main__":
    main()