"""
Shared Salesforce Bulk API v2 utilities.

Consolidates the duplicated bulk job creation, polling, and result fetching from:
  - glue_salesforce/sf_bulk_opportunity.py (lines 52-88)
  - glue_salesforce/salesforce_bulk_opportunity_etl_incremental.py (lines 98-149)
  - glue_salesforce/salesforce_bulk_opportunity_etl_pagination.py (lines 51-98)
"""

import time
import requests


API_VERSION = "v58.0"


def _auth_headers(access_token):
    return {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json",
    }


def create_query_job(instance_url, access_token, soql_query, sobject="Opportunity"):
    """
    Create a Salesforce Bulk API v2 query job.

    Returns:
        The job ID string.
    """
    headers = _auth_headers(access_token)
    payload = {
        "operation": "query",
        "query": soql_query,
        "object": sobject,
        "contentType": "JSON",
    }
    response = requests.post(
        f"{instance_url}/services/data/{API_VERSION}/jobs/query",
        headers=headers,
        json=payload,
    )
    response.raise_for_status()
    return response.json()["id"]


def poll_job_completion(instance_url, access_token, job_id, poll_interval=10):
    """
    Poll a Bulk API v2 query job until it completes.

    Returns:
        The final job state string.
    """
    headers = _auth_headers(access_token)
    status = "InProgress"
    while status in ("InProgress", "UploadComplete"):
        time.sleep(poll_interval)
        response = requests.get(
            f"{instance_url}/services/data/{API_VERSION}/jobs/query/{job_id}",
            headers=headers,
        )
        response.raise_for_status()
        status = response.json()["state"]
        print(f"Job status: {status}")
    return status


def fetch_results(instance_url, access_token, job_id):
    """
    Fetch all result records from a completed Bulk API v2 query job,
    handling pagination via the Sforce-Locator header.

    Returns:
        List of all record dicts.
    """
    headers = _auth_headers(access_token)
    results_url = f"{instance_url}/services/data/{API_VERSION}/jobs/query/{job_id}/results"
    params = {}
    all_records = []

    while True:
        response = requests.get(results_url, headers=headers, params=params)
        if response.status_code != 200:
            raise Exception(f"Failed to fetch results: {response.text}")
        chunk = response.json()
        all_records.extend(chunk)

        locator = response.headers.get("Sforce-Locator")
        if not locator or locator == "null":
            break
        params["locator"] = locator

    return all_records


def run_query(instance_url, access_token, soql_query, sobject="Opportunity", poll_interval=10):
    """
    End-to-end: create job -> poll -> fetch results.

    Returns:
        List of all record dicts.
    """
    job_id = create_query_job(instance_url, access_token, soql_query, sobject)
    poll_job_completion(instance_url, access_token, job_id, poll_interval)
    return fetch_results(instance_url, access_token, job_id)
