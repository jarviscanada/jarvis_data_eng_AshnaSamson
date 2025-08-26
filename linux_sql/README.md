# Linux Cluster Monitoring Agent

## Introduction
This project is a **Linux Resource Monitoring Agent** designed to collect, store, and analyze hardware specifications and real-time usage statistics across multiple Linux nodes within a Linux cluster. The agent automatically gathers hardware data and stores it in a **PostgreSQL** database for further analysis. The system targets **DevOps engineers**, **system administrators**, and **data analysts** who need centralized monitoring of server performance. The implementation leverages a combination of **Bash scripting** for data collection, **Docker** for containerized database deployment, **Git** for version control, and **crontab** for automated scheduling of resource usage tracking, with usage data recorded every minute in real-time.

---

## Quick Start

```bash
# Use the psql_docker script to create a PSQL instance with the given user
./scripts/psql_docker.sh create [db_username] [db_password]

# Create the host_info and host_usage tables in the database using ddl.sql
psql -h localhost -U [db_username] -d host_agent -f sql/ddl.sql
# db_password required

# Insert host hardware specifications into the database using the host_info script
./scripts/host_info.sh [psql host] [port] host_agent [db_username] [db_password]

# Insert host usage statistics into the database using the host_usage script
./scripts/host_usage.sh [psql host] [port] host_agent [db_username] [db_password]

# Automate collection of usage statistics using a crontab job
crontab -e
# In the editor which opens, add the line below to collect usage statistics every minute
* * * * * bash [full/path/to]/linux_sql/scripts/host_usage.sh [psql host] [port] host_agent [db_username] [db_password] &> /tmp/host_usage.log
```
---

## Implementation

The project follows a **client-server architecture** with three main components. The system consists of three Linux hosts acting as monitoring agents that collect hardware info and usage metrics, a centralized PostgreSQL database hosted in a Docker container to store and manage all collected data, and a cron-based scheduler to automate usage tracking at regular intervals.

- The `host_info.sh` script runs once to collect static system information (e.g., CPU cores, disk size, total memory) and stores it in the `host_info` table.
- The `host_usage.sh` script runs periodically via `crontab` to capture dynamic metrics (e.g., CPU usage, free memory, disk I/O) and inserts them into the `host_usage` table.
- The PostgreSQL database acts as a centralized repository, enabling analytics through `ddl.sql`.

---

## Architecture
<img width="895" height="1639" alt="image" src="https://github.com/jarviscanada/jarvis_data_eng_AshnaSamson/blob/develop/linux_sql/assets/architecture_diagram.png" />

---

## Scripts
The `psql_docker.sh` script manages the PSQL Docker instance

```
# Provisions and starts a PSQL Docker instance, creating the specified user within
./scripts/psql_docker.sh create [db_username] [db_password]

# Start/stop the Docker instance
./scripts/psql_docker.sh [start | stop]
# Need to ensure the Docker instance has been created
```

The `ddl.sql` script handles creating the database tables needed for the agent
```
# Create the host_info and host_usage tables in the host_agent database
psql -h localhost -U [db_username] -d host_agent -f sql/ddl.sql
# db_password required, also assumes host_agent database already exists
# Docker instance needs to be running
```

The `host_info` script gets host hardware specifications and adds them to the database
```
# Get hardware specifications and insert them into the host_info table
./scripts/host_info.sh [psql host] [port] host_agent [db_username] [db_password]
# Where: 
# psql host is the connection to the PSQL instance
# port is the port to connect to
# db_username/password are from the user created along with the instance
# Assumes that the host_info table already exists within the host_agent database
# Docker instance needs to be running
# Only needs to be run once per host
```

The `host_usage` script gets host usage statistics and adds them to the database
```
# Get usage statistics and insert them into the host_usage table
./scripts/host_usage.sh [psql host] [port] host_agent [db_username] [db_password]
# Where: 
# psql host is the connection to the PSQL instance
# port is the port to connect to
# db_username/password are from the user created along with the instance
# Assumes that the host_usage table already exists within the host_agent database
# Docker instance needs to be running
# Should be run multiple times for continuous updates
```

 The `host_usage` script can be automated using crontab jobs
 ```
crontab -e
# In the bash editor which opens, add the line below to collect usage statistics every minute
* * * * * bash [full/path/to]/linux_sql/scripts/host_usage.sh [psql host] [port] host_agent [db_username] [db_password] &> /tmp/host_usage.log
# Can ensure the crontab job is running by using:
crontab -l
# Results are logged in /tmp/host_usage.log
```

The `queries.sql` script includes analytical SQL queries to identify patterns in hardware usage and optimize resource allocation, aiding in problems such as detecting underutilized servers and predicting future hardware upgrade needs.

---

## Database Modelling
The `host_agent` database contains two tables:

The `host_info` table contains hardware specifications for the host on which the script was run

Field | Description
--- | ---
`id` | Auto-incremented unique identifier for the host
`hostname` | The name for the host, which also needs to be unique
`cpu_number` | The number of cores the CPU has
`cpu_architecture` | The architecture of the CPU
`cpu_model` | The name of the CPU model
`cpu_mhz` | The clock speed of the CPU, in MHz
`L2_cache` | The size of the L2 cache, in KB
`total_mem` | The total amount of memory in the node
`timestamp` | When these specifications were taken

The `host_usage` table contains usage statistics for the host on which the script was run

Field | Description
--- | ---
`timestamp` | When these statistics were taken
`host_id` | The ID of the corresponding host_info entry
`memory_free` | The amount of free memory in the node
`cpu_idle` | The percentage of time that the CPU is idle
`cpu_kernel` | The percentage of time the CPU is running kernel code
`disk_io` | The number of disks currently undergoing I/O processes
`disk_available` | Available space in the disk's root directory, in MB

---

## Test
The schema was verified by executing ddl.sql and confirming that the tables exist in PostgreSQL:
```psql -h localhost -U postgres -d host_agent -c "SELECT * FROM host_info;"```

---

## Deployment
GitHub: Version-controlled host scripts and SQL files.

Docker: Deployed PostgreSQL in a containerized environment.

Crontab: Configured automated scheduling for resource monitoring.

---

## Improvements
- Add additional queries that check for failed nodes or other analysis as needed.
- Add security enhancements to monitor network traffic.
- Enable `host_info.sh` to track changes in hardware configurations.
