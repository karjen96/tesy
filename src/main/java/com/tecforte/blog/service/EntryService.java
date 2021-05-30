package com.tecforte.blog.service;

import com.tecforte.blog.domain.Blog;
import com.tecforte.blog.domain.Entry;
import com.tecforte.blog.domain.enumeration.Emoji;
import com.tecforte.blog.domain.enumeration.Keyword;
import com.tecforte.blog.repository.BlogRepository;
import com.tecforte.blog.repository.EntryRepository;
import com.tecforte.blog.service.dto.EntryDTO;
import com.tecforte.blog.service.mapper.EntryMapper;
import com.tecforte.blog.web.rest.errors.BadRequestAlertException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link Entry}.
 */
@Service
@Transactional
public class EntryService {

    private final Logger log = LoggerFactory.getLogger(EntryService.class);

    private final EntryRepository entryRepository;

    private final EntryMapper entryMapper;

    @Autowired
    private BlogRepository blogRepository;

    private static final String ENTITY_NAME = "entity";

    public EntryService(EntryRepository entryRepository, EntryMapper entryMapper) {
        this.entryRepository = entryRepository;
        this.entryMapper = entryMapper;
    }

    /**
     * Save a entry.
     *
     * @param entryDTO the entity to save.
     * @return the persisted entity.
     */
    public EntryDTO save(EntryDTO entryDTO) {

        blogRepository.findById(entryDTO.getBlogId()).ifPresent(existingBlog -> {
            // question 1.1
            boolean checkBlog = checkEmoji(existingBlog, entryDTO.getEmoji());
            // question 1.2
            boolean checkTitle = checkTitle(existingBlog, entryDTO.getTitle());
            boolean checkContent = checkContent(existingBlog, entryDTO.getContent());
            if (!checkBlog) {
                throw new BadRequestAlertException("Invalid Emoji", ENTITY_NAME, "invalidEmoji");
            }
            if (!checkTitle || !checkContent) {
                throw new BadRequestAlertException("Invalid Content", ENTITY_NAME, "invalidContent");
            }
        });
        log.debug("Request to save Entry : {}", entryDTO);
        Entry entry = entryMapper.toEntity(entryDTO);
        entry = entryRepository.save(entry);
        return entryMapper.toDto(entry);
    }

    /**
     * Get all the entries.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<EntryDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Entries");
        return entryRepository.findAll(pageable).map(entryMapper::toDto);
    }

    /**
     * Get one entry by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<EntryDTO> findOne(Long id) {
        log.debug("Request to get Entry : {}", id);
        return entryRepository.findById(id).map(entryMapper::toDto);
    }

    /**
     * Delete the entry by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Entry : {}", id);
        entryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public void deleteByKeywords(String keywords) {
        List<Entry> entryList = entryRepository.findByContentIgnoreCaseContainingOrTitleIgnoreCaseContaining(keywords,
                keywords);
        entryRepository.deleteAll(entryList);
    }

    public void deleteByKeywords(Long id, String keywords) {
        List<Entry> entryList = entryRepository.findByContentIgnoreCaseContainingOrTitleIgnoreCaseContaining(keywords,
                keywords);
        log.debug("entry{}", entryList.stream().filter(o -> o.getId().equals(id)).findFirst().isPresent());

        if (entryList.stream().filter(o -> o.getId().equals(id)).findFirst().isPresent()) {
            delete(id);
        }
    }

    public boolean checkEmoji(Blog existingBlog, Emoji emoji) {
        if (existingBlog.isPositive() != null && (Emoji.LIKE == emoji || Emoji.HAHA == emoji)) {
            return false;
        }
        log.debug("blog{}, emoji{}", existingBlog.isPositive(), emoji);
        return true;
    }

    public boolean checkContent(Blog existingBlog, String content) {
        String[] words = content.split(" ");
        return checkKeyword(existingBlog.isPositive(), words);
    }

    public boolean checkTitle(Blog existingBlog, String title) {
        String[] words = title.split(" ");
        return checkKeyword(existingBlog.isPositive(), words);
    }

    public boolean checkKeyword(boolean isPositive, String[] words) {

        for (String w : words) {
            if (isPositive) {
                if (Keyword.FEAR.toString().equals(w.toUpperCase()) || Keyword.FEAR.toString().equals(w.toUpperCase())
                        || Keyword.LONELY.toString().equals(w.toUpperCase())) {
                    return false;
                }
            } else {
                if (Keyword.LOVE.toString().equals(w.toUpperCase()) || Keyword.HAPPY.toString().equals(w.toUpperCase())
                        || Keyword.TRUST.toString().equals(w.toUpperCase())) {
                    return false;
                }
            }
        }
        return true;
    }

}
