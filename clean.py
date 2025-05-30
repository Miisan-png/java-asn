import os
import re

SRC_DIR = os.path.join(os.path.dirname(__file__), 'src')

def remove_single_line_comments(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    cleaned_lines = [re.sub(r'(?<!http:)//.*', '', line) for line in lines]

    with open(file_path, 'w', encoding='utf-8') as f:
        f.writelines(cleaned_lines)

def clean_src_directory():
    for root, _, files in os.walk(SRC_DIR):
        for file in files:
            if file.endswith('.java'):
                file_path = os.path.join(root, file)
                remove_single_line_comments(file_path)

if __name__ == '__main__':
    clean_src_directory()
