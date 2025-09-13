pipeline {
    agent any

    // Define parameters for Jenkins UI
    parameters {
        choice(
            name: 'SERVICE_NAME',
            choices: ['docker', 'kubelet', 'nginx'],
            description: 'Select the service to manage'
        )
        choice(
            name: 'SERVICE_ACTION',
            choices: ['status', 'start', 'stop', 'restart'],
            description: 'Select the action to perform'
        )
        choice(
            name: 'TARGET_SERVER',
            choices: ['all', 'k8s-master-1', 'k8s-master-2', 'k8s-master-3', 
                      'k8s-infra-1', 'k8s-infra-2', 'k8s-app-1', 'k8s-app-2'],
            description: 'Select the target server'
        )
    }

    stages {
        stage('Run Ansible Service Operation') {
            steps {
                script {
                    echo "Running Jenkins job as user: ${env.USER}"
                    echo "Executing Ansible playbook via sudo as root locally on k8s-bld-1"
                    echo "Managing service '${params.SERVICE_NAME}' with action '${params.SERVICE_ACTION}' on target '${params.TARGET_SERVER}'"

                    sh """
                        cd /ansible-k8s-infra
                        # Explicit sudo execution, no password required due to NOPASSWD
                        sudo ansible-playbook -i inventory/hosts.yml playbooks/manage-service.yml \
                            -u tin \
                            -e service=${params.SERVICE_NAME} \
                            -e action=${params.SERVICE_ACTION} \
                            -e target=${params.TARGET_SERVER}
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Service operation pipeline completed successfully."
        }
        failure {
            echo "Service operation failed. Check console output for details."
        }
    }
}

