apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-service
  namespace: apps
  labels:
    app: my-service
spec:
  replicas: 2
  progressDeadlineSeconds: 600
  strategy:
    rollingUpdate:
      maxUnavailable: 0
      maxSurge: 1
    type: RollingUpdate
  selector:
    matchLabels:
      app: my-service
  template:
    metadata:
      labels:
        app: my-service
    spec:
      initContainers:
        - name: wait-for-db
          image: busybox:1.36
          command: ["sh", "-c", "until nc -z postgres.apps.svc.cluster.local 5432; do echo 'waiting for postgres'; sleep 2; done"]
      imagePullSecrets:
        - name: ghcr-creds
      containers:
        - name: my-service
          image: ${IMAGE}
          imagePullPolicy: IfNotPresent
          ports:
            - name: http
              containerPort: 8080
          envFrom:
            - configMapRef:
                name: my-service-config
            - secretRef:
                name: my-service-secrets
          env:
            - name: LOGGING_CONFIG
              value: file:/config/logback-console.xml
          resources:
            requests:
              cpu: "200m"
              memory: "256Mi"
            limits:
              cpu: "1"
              memory: "512Mi"
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 12
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
            timeoutSeconds: 2
            periodSeconds: 10
            failureThreshold: 6
          volumeMounts:
            - name: logback
              mountPath: /config
              readOnly: true
      volumes:
        - name: logback
          configMap:
            name: my-service-logback
            items:
              - key: logback-console.xml
                path: logback-console.xml
