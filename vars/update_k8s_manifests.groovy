def call(Map config = [:]) {

    // ==========================================================
    // CONFIGURATION
    // ==========================================================

    def imageTag = config.imageTag

    def manifestsPath = config.get(
        'manifestsPath',
        'kubernetes'
    )

    def gitCredentials = config.get(
        'gitCredentials',
        'github-credentials'
    )

    def gitUserName = config.get(
        'gitUserName',
        'Jenkins CI'
    )

    def gitUserEmail = config.get(
        'gitUserEmail',
        'jenkins@local'
    )

    // GitOps repository
    def gitOpsRepo =
        'https://github.com/sufiyannadeem/tws-e-commerce-gitops.git'

    // GitOps branch
    def gitOpsBranch = 'main'

    // Temporary directory inside Jenkins workspace
    def gitOpsDir = 'gitops'


    // ==========================================================
    // VALIDATION
    // ==========================================================

    if (!imageTag) {
        error("ERROR: imageTag is required")
    }


    // ==========================================================
    // GITHUB CREDENTIALS
    // ==========================================================

    withCredentials([
        usernamePassword(
            credentialsId: gitCredentials,
            usernameVariable: 'GIT_USERNAME',
            passwordVariable: 'GIT_PASSWORD'
        )
    ]) {

        sh(
            script: """
                set -e

                echo "=========================================="
                echo "GITOPS DEPLOYMENT"
                echo "=========================================="

                echo "GitOps Repository:"
                echo "${gitOpsRepo}"

                echo "GitOps Branch:"
                echo "${gitOpsBranch}"

                echo "Image Tag:"
                echo "${imageTag}"

                echo "=========================================="
                echo "Cleaning old GitOps directory"
                echo "=========================================="

                rm -rf "${gitOpsDir}"


                echo "=========================================="
                echo "Cloning GitOps repository"
                echo "=========================================="

                git clone \
                    --branch "${gitOpsBranch}" \
                    "https://\${GIT_USERNAME}:\${GIT_PASSWORD}@github.com/sufiyannadeem/tws-e-commerce-gitops.git" \
                    "${gitOpsDir}"


                cd "${gitOpsDir}"


                echo "=========================================="
                echo "Git repository information"
                echo "=========================================="

                git branch
                git log -1 --oneline


                echo "=========================================="
                echo "Configuring Git identity"
                echo "=========================================="

                git config user.name "${gitUserName}"
                git config user.email "${gitUserEmail}"


                echo "=========================================="
                echo "Checking Kubernetes manifests"
                echo "=========================================="

                if [ ! -d "${manifestsPath}" ]; then
                    echo "ERROR: Kubernetes manifests directory not found:"
                    echo "${manifestsPath}"
                    exit 1
                fi


                echo "=========================================="
                echo "Updating EasyShop application image"
                echo "=========================================="

                find "${manifestsPath}" -type f -name "*.yaml" -exec sed -i \
                    's|image: sufiyannadeem/easyshop-app:.*|image: sufiyannadeem/easyshop-app:${imageTag}|g' {} \\;


                echo "=========================================="
                echo "Updating EasyShop migration image"
                echo "=========================================="

                find "${manifestsPath}" -type f -name "*.yaml" -exec sed -i \
                    's|image: sufiyannadeem/easyshop-migration:.*|image: sufiyannadeem/easyshop-migration:${imageTag}|g' {} \\;


                echo "=========================================="
                echo "Git diff"
                echo "=========================================="

                git diff


                echo "=========================================="
                echo "Checking for changes"
                echo "=========================================="

                if git diff --quiet; then
                    echo "No Kubernetes manifest changes detected."
                    echo "Nothing to commit."
                    exit 0
                fi


                echo "=========================================="
                echo "Adding Kubernetes manifests"
                echo "=========================================="

                git add "${manifestsPath}/"


                echo "=========================================="
                echo "Creating Git commit"
                echo "=========================================="

                git commit \
                    -m "chore: update image tags to ${imageTag}"


                echo "=========================================="
                echo "Pushing GitOps changes"
                echo "=========================================="

                git push origin "${gitOpsBranch}"


                echo "=========================================="
                echo "GITOPS UPDATE SUCCESSFUL"
                echo "=========================================="

                echo "Image tag ${imageTag} pushed to GitOps repository."
                echo "Argo CD will now synchronize the deployment."

            """,
            label: 'Update GitOps Kubernetes manifests'
        )
    }
}
