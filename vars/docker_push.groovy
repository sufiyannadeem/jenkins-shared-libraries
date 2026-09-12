def call(Map config = [:]) {

    def imageName = config.imageName ?: error("Image name is required")
    def imageTag = config.imageTag ?: 'latest'
    def credentials = config.credentials ?: 'dockerhub-credentails'

    echo "Pushing Docker image: ${imageName}:${imageTag}"

    withCredentials([
        usernamePassword(
            credentialsId: credentials,
            usernameVariable: 'DOCKER_USERNAME',
            passwordVariable: 'DOCKER_PASSWORD'
        )
    ]) {

        withEnv([
            "IMAGE_NAME=${imageName}",
            "IMAGE_TAG=${imageTag}"
        ]) {

            sh '''
                set -e

                echo "$DOCKER_PASSWORD" | docker login \
                    -u "$DOCKER_USERNAME" \
                    --password-stdin

                docker push "$IMAGE_NAME:$IMAGE_TAG"
                docker push "$IMAGE_NAME:latest"
            '''
        }
    }
}


