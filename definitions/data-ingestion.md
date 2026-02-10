# Data Ingestion

## Apache NiFi

Apache NiFi is a dataflow automation platform designed to move, route, transform, and manage data between systems in real time. It provides a web-based UI for visually designing data pipelines as directed graphs of processors, each handling a specific task (fetch, parse, filter, route, deliver). NiFi supports back-pressure, guaranteed delivery, data provenance tracking (built-in lineage), and pluggable connectors for databases, file systems, APIs, message brokers, and more — making it a strong fit for the ingestion layer of this kata where data must be pulled from a relational DB, a file system, and a WS-* service.
