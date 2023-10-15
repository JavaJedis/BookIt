#Check and install libraries
import pip, json, os, time
try:
    import requests, bs4, html5lib
except ModuleNotFoundError:
    print("Installing Libraries...")
    pip.main(["install", "requests", "html5lib", "bs4"])

#Global Definitions
DEPT_LIST_URL = "https://courses.students.ubc.ca/cs/courseschedule?pname=subjarea&tname=subj-all-departments"
COURSE_LIST_BASE_URL = "https://courses.students.ubc.ca/cs/courseschedule?pname=subjarea&tname=subj-department&dept="
SECTION_LIST_BASE_URL = "https://courses.students.ubc.ca/cs/courseschedule?pname=subjarea&tname=subj-course&dept="
SECTION_BASE_URL = "https://courses.students.ubc.ca/cs/courseschedule?pname=subjarea&tname=subj-section&dept="

#Course Definition
class Subject:

    def __init__(self, subj_code: str, subj_title: str, dept_name: str):
        self.code = subj_code
        self.title = subj_title
        self.dept_name = dept_name

    def to_json(self) -> json:
        subj_dict = dict()
        subj_dict["subject_code"] = self.code
        subj_dict["subject_title"] = self.title
        subj_dict["subject_dept"] = self.dept_name
        return json.dumps(subj_dict)


class Course:

    def __init__(self, course_code: str, course_name: str):
        self.code = course_code
        self.name = course_name

    def to_json(self) -> json:
        course_dict = dict()
        course_dict["course_code"] = self.code
        course_dict["course_name"] = self.name
        return json.dumps(course_dict)

class Section:

    def __init__(self, section_code: str, section_term: list, 
                 section_days: list, start_time: list, end_time: list,
                 building: list, room: list):
        self.code = section_code
        self.term = section_term
        self.days = section_days
        self.start_time = start_time
        self.end_time = end_time
        self.building = building
        self.room = room

    def to_json(self) -> json:
        section_dict = {"section_code": self.code, "section_term": self.term,
                        "section_days": self.days, "section_start_time": self.start_time, 
                        "section_end_time": self.end_time, "section_building": self.building, 
                        "section_room": self.room}
        return json.dumps(section_dict)
    

def get_subj_list() -> list:
    response = requests.get(DEPT_LIST_URL)
    data = bs4.BeautifulSoup(response.content, "html.parser")
    allSubjs_raw = data.tbody.find_all("tr")
    subj_list = list()
    for subj_raw in allSubjs_raw:
        data_cells = subj_raw.find_all("td")
        if data_cells[0].a == None:
            continue
        subject = Subject(data_cells[0].a.text.strip(), data_cells[1].text.strip(), data_cells[2].text.strip())
        subj_list.append(subject)    
    return subj_list

def get_course_list(subj: Subject) -> list:
    course_list = list()
    course_url = COURSE_LIST_BASE_URL + subj.code
    response = requests.get(course_url)
    course_list_raw = bs4.BeautifulSoup(response.content, "html.parser").tbody.find_all("tr")
    for course_raw in course_list_raw:
        data_cells = course_raw.find_all("td")
        course = Course(data_cells[0].a.text.strip(), data_cells[1].text.strip())
        course_list.append(course)
    return course_list

def get_section_code_list(course: Course) -> list:
    section_list = list()
    course_code_detailed = course.code.split(sep=" ")
    course_subj = course_code_detailed[0]
    course_num = course_code_detailed[1]
    section_url = SECTION_LIST_BASE_URL + course_subj + "&course=" + course_num
    response = requests.get(section_url)
    section_list_raw = bs4.BeautifulSoup(response.content, "html.parser").find_all("table", class_="table table-striped section-summary")[0].find_all("tr")[1:]
    for section_raw in section_list_raw:
        data_cells = section_raw.find_all("td")
        if data_cells[1].a == None:
            continue
        section_list.append(data_cells[1].a.text.strip())
    return section_list    

def get_section_details(section_code: str) -> Section:
    section_splitted = section_code.split(sep=" ")
    url = SECTION_BASE_URL + section_splitted[0] + "&course=" + section_splitted[1] + "&section=" + section_splitted[2]
    response = requests.get(url)
    section_data_raw = bs4.BeautifulSoup(response.content, "html.parser").find_all("table", class_="table table-striped")
    if len(section_data_raw) == 0:
        return None
    all_entries = section_data_raw[0].find_all("td")
    section: Section = Section(section_code, list(), list(), list(), list(), list(), list())
    for i in range(0, len(all_entries), 6):
        if all_entries[5].text.strip() == "":
            continue
        section.term.append(all_entries[0].text.strip())
        section.days.append(all_entries[1].text.strip())
        section.start_time.append(all_entries[2].text.strip())
        section.end_time.append(all_entries[3].text.strip())
        section.building.append(all_entries[4].text.strip())
        try:
            section.room.append(all_entries[5].a.text.strip())
        except AttributeError:
            section.room.append(all_entries[5].text.strip())
    return section

subj_list = get_subj_list()
count = 0
current_time = time.monotonic()
result = dict()
print("Start Working..")
for subj in subj_list:
    course_list = get_course_list(subj)
    for course in course_list:
        section_list = get_section_code_list(course)
        count += 1
        for section in section_list:
            section_data = get_section_details(section)
            if section_data != None:
                for i in range(0, len(section_data.building)):
                    section_dict = {"section_code": section_data.code, "section_term": section_data.term[i],
                        "section_days": section_data.days[i], "section_start_time": section_data.start_time[i], 
                        "section_end_time": section_data.end_time[i], "section_building": section_data.building[i], 
                        "section_room": section_data.room[i]}
                    dumped_section = json.dumps(section_dict)
                    try:
                        result[section_data.building[i]].append(dumped_section)
                    except KeyError:
                        result[section_data.building[i]] = list()
                        result[section_data.building[i]].append(dumped_section)
print("Start Writing Data...")
index = 1
for building in result.keys():
    f = open("./buildings/" + str(index) + ".txt", "w")
    print(building)
    f.write(str(result[building]))
    f.flush()
    f.close()
    index += 1

print("Done")
print("Number of courses: " + str(count))
print("Time spent: " + str(time.monotonic() - current_time))