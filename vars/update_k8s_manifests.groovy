def call(
    String imageTag,
    String manifestsPath = 'kubernetes',
    String gitCredentials = 'github-credentials',
    String gitUserName = 'Jenkins CI',
    String gitUserEmail = 'sufiyanmohammed098@gmail.com'
) {

    def gitOpsRepo = 'https://github.com/sufiyannadeem/tws-e-commerce-gitops.git'
    def gitOpsBranch = 'main'
    def gitOpsDir = 'gitops'

    withCredentials([
        usernamePassword(
            credentialsId: gitCredentials,
            usernameVariable: 'GIT_USERNAME',
            passwordVariable: 'GIT_PASSWORD'
        )
    ]) {

        sh """
            set -e

            echo "Cloning GitOps repository..."

            rm -rf ${gitOpsDir}

            git clone --branch ${gitOpsBranch} https://\${GIT_USERNAME}:\${GIT_PASSWORD}@github.com/sufiyannadeem/tws-e-commerce-gitops.git ${gitOpsDir}

            cd ${gitOpsDir}

            git config user.name "${gitUserName}"
            git config user.email "${gitUserEmail}"

            echo "Updating EasyShop image tags to ${imageTag}"

            sed -i 's|image: sufiyannadeem/easyshop-app:.*|image: sufiyannadeem/easyshop-app:${imageTag}|g' ${manifestsPath}/*.yaml

            sed -i 's|image: sufiyannadeem/easyshop-migration:.*|image: sufiyannadeem/easyshop-migration:${imageTag}|g' ${manifestsPath}/*.yaml

            echo "Checking changes..."

            git status

            if git diff --quiet; then
                echo "No Kubernetes manifest changes detected."
                exit 0
            fi

            echo "Committing GitOps changes..."

            git add ${manifestsPath}/

            git commit -m "chore: update image tags to ${imageTag}"

            echo "Pushing changes to GitOps repository..."

            git push origin ${gitOpsBranch}

            echo "GitOps repository updated successfully."
        """
    }
}
