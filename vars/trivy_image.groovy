def call(String imageName, String imageTag) {
    echo "Scanning Docker image: ${imageName}:${imageTag}"

    sh """
        trivy image \
          --severity HIGH,CRITICAL \
          --exit-code 1 \
          ${imageName}:${imageTag}
    """

    echo "Docker image scan completed successfully"
}
