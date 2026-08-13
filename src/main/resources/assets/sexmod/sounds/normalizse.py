import subprocess
import os


def normalize_ogg_files(directory="."):
    normalized_folder = os.path.join(directory, "normalized")

    # Create the "normalized" folder if it doesn't exist
    if not os.path.exists(normalized_folder):
        os.makedirs(normalized_folder)

    for filename in os.listdir(directory):
        if filename.endswith(".ogg"):
            input_path = os.path.join(directory, filename)

            # Construct the output path in the "normalized" folder
            output_path = os.path.join(normalized_folder, filename)

            # ffmpeg command for normalization
            command = [
                "ffmpeg",
                "-i", input_path,
                "-af", "loudnorm",
                "-y",  # Overwrite output file if it already exists
                output_path
            ]

            subprocess.run(command)
            print(f"Normalized {filename} and saved to 'normalized' folder")


if __name__ == "__main__":
    normalize_ogg_files()
