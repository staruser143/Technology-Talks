This traces the exact path from the "subscription workflow" box in the first diagram. A couple of things worth noting that the boxes can't fully capture:

- The **EventBridge event** ("Subscription Request Created") is the integration point — if you need custom logic (e.g. auto-approving certain asset types, or routing unmanaged-asset requests to a manual process), this is where you'd hook in a Lambda function.
- The **Lake Formation grant** step only fires cleanly if the underlying asset is Lake Formation-managed. For unmanaged assets, approval still happens in DataZone but no query access is actually granted — that edge case sits outside this happy-path diagram.
- **Environment provisioning** is what makes the asset show up in the consuming project's Athena `_sub_db` (or equivalent Redshift access) — that's the step that connects back to the "Amazon Athena / Redshift" consumption tools in the first diagram.
