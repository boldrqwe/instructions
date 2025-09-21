# k8s/deployment.yaml
apiVersion: apps/v1                       # Deployment API: https://k8s.io/docs/concepts/workloads/controllers/deployment/
kind: Deployment
metadata:
  name: my-service
  namespace: apps
  labels:                                  # Labels: https://k8s.io/docs/concepts/overview/working-with-objects/labels/
    app: my-service
spec:
  replicas: 2                              # Кол-во подов: https://k8s.io/docs/concepts/workloads/controllers/deployment/#replicas
  revisionHistoryLimit: 5                  # Сколько ревизий хранить для rollback: https://k8s.io/docs/concepts/workloads/controllers/deployment/#revision-history-limit
  strategy:                                # Стратегия обновления: https://k8s.io/docs/concepts/workloads/controllers/deployment/#strategy
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 0                    # Без потери capacity
      maxSurge: 1
  selector:                                # Какие поды принадлежат Deployment: https://k8s.io/docs/concepts/overview/working-with-objects/labels/#label-selectors
    matchLabels:
      app: my-service
  template:                                # Шаблон подов: https://k8s.io/docs/concepts/workloads/pods/
    metadata:
      labels:
        app: my-service
    spec:
      serviceAccountName: default          # SA: https://k8s.io/docs/tasks/configure-pod-container/configure-service-account/
      securityContext:                     # Под/контейнер security: https://k8s.io/docs/tasks/configure-pod-container/security-context/
        runAsNonRoot: true
        runAsUser: 10001
        runAsGroup: 10001
        fsGroup: 10001
      containers:
        - name: my-service
          image: ${IMAGE}  # Контейнерный образ
          imagePullPolicy: IfNotPresent                  # Политика загрузки: https://k8s.io/docs/concepts/containers/images/#image-pull-policy
          ports:
            - name: http
              containerPort: 8080
          envFrom:                                      # Импорт env из ConfigMap/Secret: https://k8s.io/docs/tasks/configure-pod-container/configure-pod-configmap/#use-environment-variables-in-container-command-and-arguments
            - configMapRef:
                name: my-service-config
            - secretRef:
                name: my-service-secrets
          readinessProbe:                               # ReadinessProbe: https://k8s.io/docs/concepts/workloads/pods/pod-lifecycle/#container-probes
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 15
            periodSeconds: 5
            timeoutSeconds: 2
            failureThreshold: 6
          livenessProbe:                                # LivenessProbe: https://k8s.io/docs/concepts/workloads/pods/pod-lifecycle/#container-probes
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
            timeoutSeconds: 2
          resources:                                    # Requests/Limits: https://k8s.io/docs/concepts/configuration/manage-resources-containers/
            requests:
              cpu: "200m"
              memory: "256Mi"
            limits:
              cpu: "1"
              memory: "512Mi"
