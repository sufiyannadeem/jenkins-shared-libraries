def call() {
    echo "Running Trivy filesystem security scan..."

    sh '''
        trivy fs \
          --scanners vuln,secret \
          --severity HIGH,CRITICAL \
          --exit-code 1 \
          .
    '''

    echo "Trivy filesystem scan completed successfully"
}
