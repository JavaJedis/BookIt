from bs4 import BeautifulSoup
import requests
import json

url = "https://learningspaces.ubc.ca/find-space/informal-learning-spaces"

response = requests.get(url)

soup = BeautifulSoup(response.text, "html.parser")

# find divs with name, address, capacity, description, and pictures
panels = soup.find_all(name="div", class_="views-field views-field-rendered-entity")

print("start")
ils_info = []

for panel in panels:

    name_raw = panel.find(name="h2").getText()
    name = name_raw.replace("\u00e9", "")
    
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
    
    # add dictionary inside ils_info list
    dict = {
        "name": name,
        "address": address,
        "capacity": capacity,
        "description": description,
        "image_url": img_url
    }
    
    ils_info.append(dict)

print(ils_info)

# save the data as JSON
with open("ils_info.json", "w") as json_file:
    json.dump(ils_info, json_file, indent=4)