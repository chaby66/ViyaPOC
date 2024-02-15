import json
import requests
from requests.auth import HTTPBasicAuth

session = requests.Session()
headers = {"Content-Type": "application/json; charset=utf-8"}

# the input parameters to be generated during migration
def execute(ge_url, ge_username, ge_pass, sp_name, correlation_id,
            {INPUT_PARAMETERS}):
    'Output: {INPUT_PARAMETERS}' # the output parameters to be generated during migration
    session.auth = HTTPBasicAuth(ge_username, ge_pass)
    session.trust_env = False

    inputs = {}
    # the input assignments to be generated during migration
    {INPUT_PARAMETER_ASSIGNS}

    param = {'correlationId': correlation_id, 'clientTimeZone': 'UTC', 'version': 1, 'inputs': inputs}

    data = {}
    resp = session.post(ge_url + sp_name, json=param, headers=headers, verify='otp_truststore.pem')
    if resp.status_code == 200:
        data = resp.content.decode('utf-8')
    else:
        raise Exception('Error: HTTP status code:', resp.status_code)

    tmp = json.loads(data)
    if 'outputs' in tmp:
        out = tmp['outputs']
        # the output assignments to be generated during migration
        {OUTPUT_PARAMETER_ASSIGNS}