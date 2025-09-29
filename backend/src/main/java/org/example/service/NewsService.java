package org.example.service;

import org.example.dto.CreateNewsRequest;
import org.example.dto.NewsResponse;
import org.example.entity.News;
import org.example.exception.NewsNotFoundException;
import org.example.repository.NewsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NewsService {
    private final NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public NewsResponse createNews(CreateNewsRequest request) {
        News news = new News();
        news.setTitle(request.getTitle());
        news.setDescription(request.getDescription());
        news.setNewsDate(request.getNewsDate());

        News savedNews = newsRepository.save(news);
        return convertToResponse(savedNews);
    }

    public void deleteNews(Long id) {
        if (!newsRepository.existsById(id)) {
            throw new NewsNotFoundException("News not found with id: " + id);
        }
        newsRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<NewsResponse> getAllNews() {
        return newsRepository.findByOrderByNewsDateDescCreatedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NewsResponse getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new NewsNotFoundException("News not found with id: " + id));
        return convertToResponse(news);
    }

    private NewsResponse convertToResponse(News news) {
        NewsResponse response = new NewsResponse();
        response.setId(news.getId());
        response.setTitle(news.getTitle());
        response.setDescription(news.getDescription());
        response.setNewsDate(news.getNewsDate());
        response.setCreatedAt(news.getCreatedAt());
        return response;
    }
}