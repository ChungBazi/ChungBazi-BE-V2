-- 정책 본 검색의 제목·요약·지원 내용·기관명 ngram FULLTEXT 인덱스
-- 4필드 검색 코드 배포 전에 개발/운영 DB에 각각 적용한다.
ALTER TABLE policy
ADD FULLTEXT INDEX ft_policy_search (title, summary, support_content, organization_name)
WITH PARSER ngram;
