package com.sigma.pumpya.api.controller

import com.sigma.pumpya.api.request.CreateTagRequest
import com.sigma.pumpya.api.response.CreateTagResponse
import com.sigma.pumpya.api.response.DeleteTagResponse
import com.sigma.pumpya.api.response.GetTagsResponse
import com.sigma.pumpya.domain.entity.Tags
import com.sigma.pumpya.infrastructure.repository.TagRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Tag Api")
@RestController("/tag")
class TagController(
    private val tagRepository: TagRepository
) {
    @Operation(summary = "get tag list")
    @GetMapping("/get-tags")
    fun getAllTags(): GetTagsResponse {
        val tagList = tagRepository.findAll() ?: emptyList();
        var response: List<String> = listOf()
        if(tagList.isNotEmpty()) {
            for(tag in tagList) {
                response.addLast(tag.tagName)
            }
        }

        return GetTagsResponse(response)
    }

    @Operation(summary = "create tag with tag name")
    @PostMapping("/create-tag")
    fun createTag(
        @Valid @RequestBody createTagRequest: CreateTagRequest
    ): CreateTagResponse {
        val tag = Tags(tagName=createTagRequest.tagName)
        val res = tagRepository.save(tag)

        return CreateTagResponse(res.tagName)
    }

    @Operation(summary = "delete tag with tag name")
    @PostMapping("/delete-tag/{tag}")
    fun deleteTag(
        @Valid @PathVariable tag: String
    ): DeleteTagResponse {
        val res =  tagRepository.deleteTagByTagName(tag)
        return DeleteTagResponse(res)
    }
}