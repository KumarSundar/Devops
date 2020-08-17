package com.mihneacristian.project_tracker.Services;

import com.mihneacristian.project_tracker.DTO.ItemDTO;
import com.mihneacristian.project_tracker.DTO.ProjectDTO;
import com.mihneacristian.project_tracker.Entities.*;
import com.mihneacristian.project_tracker.EntityConverter.ItemEntityConverter;
import com.mihneacristian.project_tracker.EntityConverter.ProjectEntityConverter;
import com.mihneacristian.project_tracker.Repositories.ItemRepository;
import com.mihneacristian.project_tracker.Repositories.StatusRepository;
import com.mihneacristian.project_tracker.Repositories.TeamMembersRepository;
import com.mihneacristian.project_tracker.Repositories.TypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    StatusRepository statusRepository;

    @Autowired
    TeamMembersRepository teamMembersRepository;

    @Autowired
    TypeRepository typeRepository;

    @Transactional
    public Boolean isItemIdPresent(Integer id) {

        return itemRepository.findById(id).isPresent();
    }

    @Transactional
    public Item findByItemId(Integer id) {

        Optional<Item> byId = itemRepository.findByItemId(id);

        if(byId.isPresent()){
            return byId.get();
        } else {
            throw new RuntimeException("Could not find a project with the id: " + id);
            // todo ELSE throw exception
        }
    }

    @Transactional
    public List<Item> findByTitle(String title) {

        return itemRepository.findByTitle(title);
    }

    @Transactional
    public List<ItemDTO> getAllItems() {

        List<Item> allItems = itemRepository.findAll();
        List<ItemDTO> itemDTOS = new ArrayList<>();

        for (Item item :
                allItems) {
            ItemDTO temp = new ItemDTO();
            temp.itemId = item.getItemId();
            temp.title = item.getTitle();
            temp.description = item.getDescription();
            temp.statusOfItem = item.getStatusOfItem().getStatusName();
            temp.typeOfItem = item.getTypeOfItem().getName();
            temp.teamMemberId = item.getTeamMemberOfItem().getMemberId();
            temp.teamMemberOfProjectLastName = item.getTeamMemberOfItem().getLastName();
            temp.teamMemberOfProjectFirstName = item.getTeamMemberOfItem().getFirstName();
            temp.teamMemberOfProjectEmailAddress = item.getTeamMemberOfItem().getEmailAddress();
            itemDTOS.add(temp);
        }
        return itemDTOS;
    }

    @Transactional
    public Item saveNewItem(ItemDTO itemDTO) {

        Optional<Status> byStatusName = statusRepository.findByStatusName(itemDTO.statusOfItem);
        Optional<Type> byTypeName = typeRepository.findByName(itemDTO.typeOfItem);
        Optional<TeamMembers> byMemberId = teamMembersRepository.findByMemberId(itemDTO.teamMemberId);

        TeamMembers teamMembers = null;
        if (!byMemberId.isPresent()) {

            TeamMembers newMember = new TeamMembers(itemDTO.teamMemberId);
            teamMembers = teamMembersRepository.save(newMember);
        } else {
            teamMembers = byMemberId.get();
        }

        Status status = null;
        if (!byStatusName.isPresent()) {

            Status newStatus = new Status(itemDTO.statusOfItem);
            status = statusRepository.save(newStatus);
        } else {

            status = byStatusName.get();
        }

        Type type = null;
        if (!byTypeName.isPresent()) {
            Type newType = new Type(itemDTO.typeOfItem);
            newType = typeRepository.save(newType);
        } else {

            type = byTypeName.get();
        }

        Item itemToBeSaved = new Item(itemDTO, teamMembers, status, type);
        Item savedItem = itemRepository.save(itemToBeSaved);
        return savedItem;
    }

    @Transactional
    public Item updateItemById(Integer id, ItemDTO itemToBeUpdated) {

        Item item;

        TeamMembers teamMembers;
        Optional<TeamMembers> teamMembersOptional = teamMembersRepository.findById(id);


        Optional<Item> itemOptional = itemRepository.findById(id);
        if (!itemOptional.isPresent()) {

            throw new RuntimeException("Could not find item with the id: " + id);
        } else {

            item = itemOptional.get();
            teamMembers = teamMembersOptional.get();

            item.setTitle(itemToBeUpdated.title);
            item.setDescription(itemToBeUpdated.description);
        }
        return itemRepository.save(item);
    }

    @Transactional
    public void deleteItemById(Integer id) {

        itemRepository.deleteById(id);
    }
}
