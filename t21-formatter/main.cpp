#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <iomanip>
#include <cstdlib>
#include <string>
#include <stdexcept>
#include <unordered_map>

std::vector<int> splitVersion(const std::string& version) {
    std::vector<int> parts;
    size_t start = 0, end;
    while ((end = version.find('.', start)) != std::string::npos) {
        parts.push_back(std::stoi(version.substr(start, end - start)));
        start = end + 1;
    }
    parts.push_back(std::stoi(version.substr(start)));
    if (parts.size() != 3) throw std::invalid_argument("[Error] Invalid version format");
    return parts;
}

std::string versionToHex(const std::string& version) {
    auto parts = splitVersion(version);
    std::ostringstream oss;
    
    for (int part : parts) {
        oss << std::setw(4) << std::setfill('0') << std::hex << std::uppercase << part;
    }
    return oss.str();
}

std::unordered_map<std::string, int> readManufacturers(const std::string& filename) {
    std::unordered_map<std::string, int> map;
    std::ifstream infile(filename);
    if (!infile) {
        throw std::runtime_error("[Error] Could not open " + filename);
    }
    std::string line;
    std::getline(infile, line);
    while (std::getline(infile, line)) {
        std::istringstream ss(line);
        std::string id_str, name;
        if (!std::getline(ss, id_str, ';')) continue;
        if (!std::getline(ss, name, ';')) continue;
        int id = std::stoi(id_str);
        map[name] = id;
    }
    return map;
}

std::unordered_map<std::string, int> readModels(const std::string& filename) {
    std::unordered_map<std::string, int> map;
    std::ifstream infile(filename);
    if (!infile) {
        throw std::runtime_error("[Error] Could not open " + filename);
    }
    std::string line;
    std::getline(infile, line);
    while (std::getline(infile, line)) {
        std::istringstream ss(line);
        std::string id_str, name, manufacturerid;
        if (!std::getline(ss, id_str, ';')) continue;
        if (!std::getline(ss, name, ';')) continue;
        if (!std::getline(ss, manufacturerid, ';')) continue;
        int id = std::stoi(id_str);
        map[manufacturerid + "#" + name] = id;
    }
    return map;
}

int main() {
    std::unordered_map<std::string, int> manufacturerMap;
    try {
        manufacturerMap = readManufacturers("manufacturers.csv");
    } catch (const std::exception& e) {
        std::cerr << e.what() << std::endl;
        return 1;
    }

    std::unordered_map<std::string, int> modelMap;
    try {
        modelMap = readModels("models.csv");
    } catch (const std::exception& e) {
        std::cerr << e.what() << std::endl;
        return 1;
    }

    std::ifstream infile("input.csv");
    if (!infile) {
        std::cerr << "\n[Error] Could not open input.csv" << std::endl;
        return 1;
    }

    std::string line;
    std::getline(infile, line);

    while (std::getline(infile, line)) {
        std::istringstream ss(line);
        std::string field;
        std::vector<std::string> fields;
        while (std::getline(ss, field, ';')) {
            fields.push_back(field);
        }
        if (fields.size() != 4) {
            std::cerr << "\n[Error] Invalid line: " << line << std::endl;
            continue;
        }
        int idInt = std::stoi(fields[0]);
        std::ostringstream idFieldStream;
        idFieldStream << std::setw(4) << std::setfill('0') << std::hex << std::uppercase << idInt;
        fields[0] = idFieldStream.str();

        auto it = manufacturerMap.find(fields[1]);
        if (it == manufacturerMap.end()) {
            std::cerr << "\n[Error] Manufacturer not found: " << fields[1] << " in line: " << line << std::endl;
            continue;
        }
        int manufacturerIdInt = it->second;
        std::ostringstream manufacturerIdStream;
        manufacturerIdStream << std::setw(4) << std::setfill('0') << std::hex << std::uppercase << manufacturerIdInt;
        std::string manufacturerIdStr = manufacturerIdStream.str();
        fields[1] = manufacturerIdStr;

        std::string modelKey = std::to_string(manufacturerIdInt) + "#" + fields[2];
        auto mit = modelMap.find(modelKey);
        if (mit == modelMap.end()) {
            std::cerr << "\n[Error] Model not found: " << fields[2] << " for manufacturer id: " << manufacturerIdInt << " in line: " << line << std::endl;
            continue;
        }
        int modelIdInt = mit->second;
        std::ostringstream modelIdStream;
        modelIdStream << std::setw(4) << std::setfill('0') << std::hex << std::uppercase << modelIdInt;
        fields[2] = modelIdStream.str();

        try {
            fields[3] = versionToHex(fields[3]);
        } catch (const std::exception& e) {
            std::cerr << "\n[Error] Invalid version format in line: " << line << " (" << e.what() << ")" << std::endl;
            continue;
        }

        std::string formatted = fields[0] + fields[1] + fields[2] + fields[3];

        std::cout << "\nSending formatted line: " << formatted << std::endl;
        std::string cmd = "java -cp jms-sender org.interview.client.ClientApplication \"" + formatted + "\"";
        int ret = std::system(cmd.c_str());
        if (ret != 0) {
            std::cerr << "[Error] Failed to call ClientApplication for line: " << line << std::endl;
        }
    }
    return 0;
}