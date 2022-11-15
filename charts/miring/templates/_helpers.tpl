{{/*
Expand the name of the chart.
*/}}
{{- define "cra-frontend.name" -}}
{{- default .Chart.Name .Values.nmdp.nameoverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "cra-frontend.fullname" -}}
{{- if .Values.nmdp.fullnameoverride }}
{{- .Values.nmdp.fullnameoverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nmdp.nameoverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "cra-frontend.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "cra-frontend.labels" -}}
helm.sh/chart: {{ include "cra-frontend.chart" . }}
{{ include "cra-frontend.selectorLabels" . }}
{{- if .Chart.AppVersion }}
nmdp.org/version: {{ .Chart.AppVersion | quote }}
{{- end }}
nmdp.org/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "cra-frontend.selectorLabels" -}}
nmdp.org/name: {{ include "cra-frontend.name" . }}
nmdp.org/instance: {{ .Release.Name }}
nmdp.org/environment: {{ .Values.nmdp.environment }}
nmdp.org/team: {{ .Values.nmdp.team }}
{{- end }}

{{/*
Define application namespace
*/}}
{{- define "cra-frontend.namespace" -}}
{{- $ns := .Values.nmdp.namespace -}}
{{- if $ns }}
{{- printf "%s" .Values.nmdp.namespace -}}
{{ else }}
{{- printf "%s-%s" (include "cra-frontend.name" .) .Values.nmdp.environment -}}
{{ end }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "cra-frontend.serviceaccountname" -}}
{{- if .Values.serviceaccount.required }}
{{- default (include "cra-frontend.name" .) .Values.serviceaccount.name }}
{{- else }}
{{- default "default" ( .Chart.Name ) }}
{{- end }}
{{- end }}
