FROM python:3.9

# Set working directory
WORKDIR /model

# Copy requirements.txt file into container
COPY requirements.txt .

# Install dependencies from requirements.txt
RUN pip install -r requirements.txt

# Copy script files into container
COPY . /model

# Run the main script
CMD ["python", "-u", "main.py"]