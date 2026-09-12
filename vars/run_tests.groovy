def call() {
    echo "Running application validation..."

    sh '''
        npm ci
        npm run lint
        npm run build
    '''

    echo "Application validation completed successfully"
}
