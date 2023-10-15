from bs4 import BeautifulSoup
import requests
import json
import re
import time

url = "https://learningspaces.ubc.ca/find-space/informal-learning-spaces"

response = requests.get(url)

soup = BeautifulSoup(response.text, "html.parser")

# find divs with name, address, capacity, description, and pictures
panels = soup.find_all(name="div", class_="views-field views-field-rendered-entity")

print("start")
ils_data = {}

building_data = []
unique_building_codes = []
address_list = []

for panel in panels:

    name_raw = panel.find(name="h2").getText()
    name = name_raw.replace("\u00e9", "")
    pattern = r'\(([^)]+)\)'
    match = re.search(pattern, name)
    building_code = match.group(1)
    
    # address and capacity are combined, need to split them apart
    address_capacity = panel.find(name="p").getText()
    split_strings = address_capacity.split('Capacity:')
    address = split_strings[0].strip()
    capacity = split_strings[1].strip()

    # need to get the second col-md-12 div
    description_elements = panel.find_all(name="div", class_="col-md-12")
    if len(description_elements) > 1:
        description_raw = description_elements[1].getText().strip()
        description = description_raw.replace("\u00e9", "").replace("\u00a0", "").replace("\u2019", "'")
    else:
        description = "No Description."
    
    img_tag = panel.find(name="img")
    img_url = img_tag["src"] if img_tag else ""

    # check if building_code already exists
    if building_code not in ils_data:
        ils_data[building_code] = []
    
    # add dictionary inside ils_data list
    ils_data[building_code].append({
        "name": name,
        "address": address,
        "capacity": capacity,
        "description": description,
        "image_url": img_url
    })
    
    if building_code not in unique_building_codes:
        address_list.append(address)
        unique_building_codes.append(building_code)
        building_object = {
            "building_code" : building_code,
            "building_name" : name,
            "address" : address
        }
        building_data.append(building_object)

# print(ils_data)

# from https://www.geoapify.com/tutorial/geocoding-python

api_key = "0d15897763d64011921f58f48effb6d9"

# With Batch Geocoding, you create a geocoding job by sending addresses and then, after some time, get geocoding results by job id
# You may require a few attempts to get results. Here is a timeout between the attempts - 1 sec. Increase the timeout for larger jobs.
timeout = 1

# Limit the number of attempts
maxAttempt = 10

def getLocations(locations):
    url = "https://api.geoapify.com/v1/batch/geocode/search?apiKey=" + api_key
    response = requests.post(url, json = locations)
    result = response.json()

    # The API returns the status code 202 to indicate that the job was accepted and pending
    status = response.status_code
    if (status != 202):
        print('Failed to create a job. Check if the input data is correct.')
        return
    jobId = result['id']
    getResultsUrl = url + '&id=' + jobId

    time.sleep(timeout)
    result = getLocationJobs(getResultsUrl, 0)
    latitude_list = [entry.get('lat') for entry in result]
    longitude_list = [entry.get('lon') for entry in result]
    if (result):
        for i in range(len(building_data)):
            building_data[i]["lat"] = latitude_list[i]
            building_data[i]["lon"] = longitude_list[i]
        # print(len(latitude_list))
        # print(len(longitude_list))
        print('You can also get results by the URL - ' + getResultsUrl)
    else:
        print('You exceeded the maximal number of attempts. Try to get results later. You can do this in a browser by the URL - ' + getResultsUrl)

def getLocationJobs(url, attemptCount):
    response = requests.get(url)
    result = response.json()
    status = response.status_code
    if (status == 200):
        print('The job is succeeded. Here are the results:')
        return result
    elif (attemptCount >= maxAttempt):
        return
    elif (status == 202):
        print('The job is pending...')
        time.sleep(timeout)
        return getLocationJobs(url, attemptCount + 1)

getLocations(address_list)

# print(len(building_data))

# save the data as JSON
with open("informal_learning_spaces_data.json", "w") as json_file:
    json.dump(ils_data, json_file, indent=4)

with open("informal_learning_spaces_building_data.json", "w") as json_file:
    json.dump(building_data, json_file, indent=4)