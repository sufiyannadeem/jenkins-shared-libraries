#!/usr/bin/env groovy

/**
 * Update Kubernetes manifests with new image tags
 */
def call(Map config = [:]) {

    def imageTag = config.imageTag ?: error("Image tag is required")
    def manifestsPath = config.manifestsPath ?: 'kubernetes'
    def gitCredentials = config.gitCredentials ?: 'github-credentials'
    def gitUserName = config.gitUserName ?: 'Jenkins CI'
    def gitUserEmail = config.gitUserEmail ?: 'jenkins@example.com'

    echo "Updating Kubernetes manifests with image tag: ${imageTag}"

    withCredentials([
        usernamePassword(
            credentialsId: gitCredentials,
            usernameVariable: 'GIT_USERNAME',
            passwordVariable: 'GIT_PASSWORD'
        )
    ]) {

        withEnv([
            "IMAGE_TAG=${imageTag}",
            "MANIFESTS_PATH=${manifestsPath}",
            "GIT_USER_NAME=${gitUserName}",
            "GIT_USER_EMAIL=${gitUserEmail}"
        ]) {

            sh '''
                set -e

                git config user.name "$GIT_USER_NAME"
                git config user.email "$GIT_USER_EMAIL"

                # Update main application deployment
                if [ -f "$MANIFESTS_PATH/08-easyshop-deployment.yaml" ]; then
                    sed -i \
                        "s|image: sufiyannadeem/easyshop-app:.*|image: sufiyannadeem/easyshop-app:$IMAGE_TAG|g" \
                        "$MANIFESTS_PATH/08-easyshop-deployment.yaml"
                fi

                # Update migration job if it exists
                if [ -f "$MANIFESTS_PATH/12-migration-job.yaml" ]; then
                    sed -i \
                        "s|image: sufiyannadeem/easyshop-migration:.*|image: sufiyannadeem/easyshop-migration:$IMAGE_TAG|g" \
                        "$MANIFESTS_PATH/12-migration-job.yaml"
                fi

                # Ensure ingress uses the correct domain
                if [ -f "$MANIFESTS_PATH/10-ingress.yaml" ]; then
                    sed -i \
                        "s|host: .*|host: easyshop.nadeemsufiyan.in|g" \
                        "$MANIFESTS_PATH/10-ingress.yaml"
                fi

                # Check only Kubernetes manifest changes
                if git diff --quiet -- "$MANIFESTS_PATH"; then
                    echo "No Kubernetes manifest changes detected."
                    exit 0
                fi

                git add "$MANIFESTS_PATH"

                git commit \
                    -m "chore: update image tags to $IMAGE_TAG [skip ci]"

                # Keep credentials OUT of the Git remote URL
                git remote set-url origin \
                    "https://github.com/sufiyannadeem/tws-e-commerce-app.git"

                # Temporary Git credential helper
                ASKPASS_SCRIPT="$(mktemp)"

                cat > "$ASKPASS_SCRIPT" <<'EOF'
#!/bin/sh

case "$1" in
    *Username*)
        echo "$GIT_USERNAME"
        ;;
    *Password*)
        echo "$GIT_PASSWORD"
        ;;
esac
EOF

                chmod 700 "$ASKPASS_SCRIPT"

                export GIT_ASKPASS="$ASKPASS_SCRIPT"
                export GIT_TERMINAL_PROMPT=0

                git push origin HEAD:master

                rm -f "$ASKPASS_SCRIPT"
            '''
        }
    }
}

