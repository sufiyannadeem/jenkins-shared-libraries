```groovy
def call(String imageTag) {

    def gitOpsRepo = 'https://github.com/sufiyannadeem/tws-e-commerce-gitops.git'
    def gitOpsDir = 'gitops'

    withCredentials([
        usernamePassword(
            credentialsId: 'github-credentials',
            usernameVariable: 'GIT_USERNAME',
            passwordVariable: 'GIT_PASSWORD'
        )
    ]) {

        sh """
            set -e

            rm -rf ${gitOpsDir}

            git clone https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/sufiyannadeem/tws-e-commerce-gitops.git ${gitOpsDir}

            cd ${gitOpsDir}

            git config user.name "jenkins"
            git config user.email "jenkins@local"

            echo "Updating EasyShop image tags to ${imageTag}"

            sed -i 's|image: sufiyannadeem/easyshop-app:.*|image: sufiyannadeem/easyshop-app:${imageTag}|g' kubernetes/*.yaml

            sed -i 's|image: sufiyannadeem/easyshop-migration:.*|image: sufiyannadeem/easyshop-migration:${imageTag}|g' kubernetes/*.yaml

            git status

            if git diff --quiet; then
                echo "No Kubernetes manifest changes detected."
                exit 0
            fi

            git add kubernetes/

            git commit -m "chore: update image tags to ${imageTag}"

            git push origin master
        """
    }
}
```
