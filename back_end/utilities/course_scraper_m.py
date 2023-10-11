"""
Web Scraper Utility for BookIt developed by Java Jedis
"""
#Libraries
import threading, json, os, time, requests, bs4, typing, random

#Global definitions
DEPT_LIST_URL = "https://courses.students.ubc.ca/cs/courseschedule?pname=subjarea&tname=subj-all-departments"
COURSE_LIST_BASE_URL = "https://courses.students.ubc.ca/cs/courseschedule?pname=subjarea&tname=subj-department&dept="
SECTION_LIST_BASE_URL = "https://courses.students.ubc.ca/cs/courseschedule?pname=subjarea&tname=subj-course&dept="
SECTION_BASE_URL = "https://courses.students.ubc.ca/cs/courseschedule?pname=subjarea&tname=subj-section&dept="
JOB_PER_THREAD = 2

#Data structure definitions
class Subject:
    """
    UBC subject
    """
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
    """
    UBC Course
    """
    def __init__(self, course_code: str, course_name: str):
        self.code = course_code
        self.name = course_name

    def to_json(self) -> json:
        course_dict = dict()
        course_dict["course_code"] = self.code
        course_dict["course_name"] = self.name
        return json.dumps(course_dict)
    
class Section:
    """
    UBC course section
    """
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
    
def scrape_subjects() -> typing.List[Subject]:
    """
    Returns all subjects offered in current session (i.e. 2023W)
    """
    web_response = requests.get(DEPT_LIST_URL)
    web_data = bs4.BeautifulSoup(web_response.content, "html.parser")
    subjs_data = web_data.tbody.find_all("tr")
    subj_list = list()
    for subj_data in subjs_data:
        data_entries:bs4.Tag = subj_data.find_all("td")
        if data_entries[0].a == None:
            continue
        subject = Subject(data_entries[0].a.text.strip(), data_entries[1].text.strip(), data_entries[2].text.strip())
        subj_list.append(subject)
    return subj_list

def scrape_courses_by_subject(subject:Subject) -> typing.List[Course]:
    course_list = list()
    course_url = COURSE_LIST_BASE_URL + subject.code
    response = requests.get(course_url)
    courses_data = bs4.BeautifulSoup(response.content, "html.parser").tbody.find_all("tr")
    for course_data in courses_data:
        data_entries = course_data.find_all("td")
        course = Course(data_entries[0].a.text.strip(), data_entries[1].text.strip())
        course_list.append(course)
    return course_list

def scrape_all_courses(subjects:typing.List[Subject], start_index:int, end_index:int, result_list:typing.List[Course]):
    """
    Returns all courses offered by UBC.
    """
    if (end_index - start_index + 1 <= JOB_PER_THREAD):
        for i in range(start_index, end_index + 1):
            result_list.extend(scrape_courses_by_subject(subjects[i]))
        return
    
    mid = (int)((start_index + end_index) / 2)
    subtask_a = threading.Thread(target=scrape_all_courses, args=[subjects, start_index, mid, result_list])
    subtask_b = threading.Thread(target=scrape_all_courses, args=[subjects, mid + 1, end_index, result_list])
    random.seed()
    time.sleep(random.random())
    subtask_a.start()
    random.seed()
    time.sleep(random.random())
    subtask_b.start()
    subtask_a.join()
    subtask_b.join()
    return

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


def get_sections_code_list(courses:list, start_index:int, end_index:int, code_list:list):
    if (end_index - start_index + 1 <= JOB_PER_THREAD):
        for i in range(start_index, end_index + 1):
            code_list.extend(get_section_code_list(courses[i]))
        return
    mid = (int)((start_index + end_index) / 2)
    subtask_a = threading.Thread(target=get_sections_code_list, args=[courses, start_index, mid, code_list])
    subtask_b = threading.Thread(target=get_sections_code_list, args=[courses, mid + 1, end_index, code_list])
    random.seed()
    time.sleep(random.random())
    subtask_a.start()
    random.seed()
    time.sleep(random.random())
    subtask_b.start()
    subtask_a.join()
    subtask_b.join()
    return

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

def get_sections_data_by_building(section_codes:typing.List[str], start_index:int, end_index:int, result:dict):
    if (end_index - start_index + 1 <= JOB_PER_THREAD):
        for i in range(start_index, end_index + 1):
            section_data = get_section_details(section_codes[i])
            for j in range(0, len(section_data.building)):
                section_dict = {"section_code": section_data.code, "section_term": section_data.term[j],
                            "section_days": section_data.days[j], "section_start_time": section_data.start_time[j], 
                            "section_end_time": section_data.end_time[j], "section_building": section_data.building[j], 
                            "section_room": section_data.room[j]}
                dumped_section = json.dumps(section_dict)
                try:
                    result[section_data.building[i]].append(dumped_section)
                except KeyError:
                    result[section_data.building[i]] = list()
                    result[section_data.building[i]].append(dumped_section)
        return
    mid = (int)((start_index + end_index) / 2)
    subtask_a = threading.Thread(target=get_sections_data_by_building, args=[section_codes, start_index, mid, result])
    subtask_b = threading.Thread(target=get_sections_data_by_building, args=[section_codes, mid + 1, end_index, result])
    random.seed()
    time.sleep(random.random())
    subtask_a.start()
    random.seed()
    time.sleep(random.random())
    subtask_b.start()
    subtask_a.join()
    subtask_b.join()
    return

print("Working...")
start_time = time.monotonic()
subjs = scrape_subjects()
courses_result = list()
section_code_list = list()
section_dict = dict()
scrape_all_courses(subjs, 0, len(subjs) - 1, courses_result)
print("Fetched all courses.")
get_sections_code_list(courses_result, 0, len(courses_result) - 1, section_code_list)
print("Fetched all section codes.")
get_sections_data_by_building(section_code_list, 0, len(section_code_list) - 1, section_dict)
print("Fetched all sections.")

print("Start Writing Data...")
index = 1
for building in section_dict.keys():
    f = open("./buildings/" + str(index) + ".txt", "w")
    print(building)
    f.write(str(section_dict[building]))
    f.flush()
    f.close()
    index += 1

print("Done")
print("Time spent: " + str(time.monotonic() - start_time))
