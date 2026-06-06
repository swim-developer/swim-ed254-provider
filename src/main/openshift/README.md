# SWIM ED-254 Provider, Raw YAML Deployment

## Prerequisites

- `oc` CLI authenticated to an OpenShift cluster
- Namespace `swim-demo` exists
- cert-manager installed with `swim-ca-issuer` ClusterIssuer
- `swim-ca-bundle` ConfigMap in `swim-demo` (CA certificate bundle)
- PostgreSQL deployed and accessible
- AMQP broker (Artemis) deployed and accessible
- Kafka cluster available
- RHBK (Keycloak) realm `swim` configured with client `swim-ed254-provider`

## Deploy Order

Apply the manifests in this exact order. Each step depends on the previous one.

```bash
# 1. ConfigMap (application configuration)
oc apply -f swim-ed254-provider-config.yaml -n swim-demo

# 2. Secrets (database/AMQP credentials and OIDC configuration)
oc apply -f swim-ed254-provider-secret.yaml -n swim-demo
oc apply -f swim-ed254-provider-oidc-secret.yaml -n swim-demo

# 3. Server Certificate (cert-manager creates the TLS secret, must exist before Deployment)
oc apply -f swim-ed254-provider-server-cert.yaml -n swim-demo

# 4. Services (must exist before Routes)
oc apply -f swim-ed254-provider-service.yaml -n swim-demo
oc apply -f swim-ed254-provider-internal-service.yaml -n swim-demo

# 5. Deployment (depends on ConfigMap, Secrets, and TLS secret from cert-manager)
oc apply -f swim-ed254-provider-deployment.yaml -n swim-demo

# 6. HorizontalPodAutoscaler (references the Deployment)
oc apply -f swim-ed254-provider-hpa.yaml -n swim-demo

# 7. Routes (depend on the Service)
oc apply -f swim-ed254-provider-route-http.yaml -n swim-demo
oc apply -f swim-ed254-provider-route.yaml -n swim-demo

# 8. ServiceMonitor (references the Service, requires Prometheus Operator)
oc apply -f swim-ed254-provider-servicemonitor.yaml -n swim-demo
```

## Teardown

Remove in reverse order:

```bash
oc delete -f swim-ed254-provider-servicemonitor.yaml -n swim-demo
oc delete -f swim-ed254-provider-route.yaml -n swim-demo
oc delete -f swim-ed254-provider-route-http.yaml -n swim-demo
oc delete -f swim-ed254-provider-hpa.yaml -n swim-demo
oc delete -f swim-ed254-provider-deployment.yaml -n swim-demo
oc delete -f swim-ed254-provider-internal-service.yaml -n swim-demo
oc delete -f swim-ed254-provider-service.yaml -n swim-demo
oc delete -f swim-ed254-provider-server-cert.yaml -n swim-demo
oc delete -f swim-ed254-provider-oidc-secret.yaml -n swim-demo
oc delete -f swim-ed254-provider-secret.yaml -n swim-demo
oc delete -f swim-ed254-provider-config.yaml -n swim-demo
```
